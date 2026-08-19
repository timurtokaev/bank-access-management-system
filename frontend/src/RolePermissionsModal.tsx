import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
} from 'react'

type RoleSummary = {
  id: string
  code: string
  name: string
  systemRole: boolean
  active: boolean
}

type PermissionResponse = {
  id: string
  code: string
  name: string
  description: string | null
  systemPermission: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}

type RolePermissionResponse = {
  roleId: string
  roleCode: string
  roleName: string
  permissionId: string
  permissionCode: string
  permissionName: string
  grantedAt: string
}

type ApiErrorResponse = {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors:
    | Record<string, string>
    | null
}

type RolePermissionsModalProps = {
  role: RoleSummary
  onClose: () => void
}

export default function RolePermissionsModal({
  role,
  onClose,
}: RolePermissionsModalProps) {
  const [
    availablePermissions,
    setAvailablePermissions,
  ] = useState<PermissionResponse[]>([])

  const [
    assignedPermissions,
    setAssignedPermissions,
  ] = useState<
    RolePermissionResponse[]
  >([])

  const [
    selectedPermissionId,
    setSelectedPermissionId,
  ] = useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [isGranting, setIsGranting] =
    useState(false)

  const [
    revokingPermissionId,
    setRevokingPermissionId,
  ] = useState<string | null>(null)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    void loadData()
  }, [role.id])

  async function loadData() {
    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const [
        permissionsResponse,
        assignedResponse,
      ] = await Promise.all([
        fetch(
          '/api/permissions/active',
          {
            headers: {
              Authorization:
                `Bearer ${accessToken}`,
            },
          },
        ),

        fetch(
          `/api/roles/${role.id}/permissions`,
          {
            headers: {
              Authorization:
                `Bearer ${accessToken}`,
            },
          },
        ),
      ])

      if (
        permissionsResponse.status ===
          401 ||
        assignedResponse.status === 401
      ) {
        redirectToLogin()
        return
      }

      if (
        permissionsResponse.status ===
          403 ||
        assignedResponse.status === 403
      ) {
        throw new Error(
          'Недостаточно прав для просмотра разрешений роли',
        )
      }

      if (!permissionsResponse.ok) {
        throw new Error(
          await readApiError(
            permissionsResponse,
            `Не удалось загрузить разрешения: HTTP ${permissionsResponse.status}`,
          ),
        )
      }

      if (!assignedResponse.ok) {
        throw new Error(
          await readApiError(
            assignedResponse,
            `Не удалось загрузить назначения: HTTP ${assignedResponse.status}`,
          ),
        )
      }

      const permissions:
        PermissionResponse[] =
          await permissionsResponse.json()

      const assignments:
        RolePermissionResponse[] =
          await assignedResponse.json()

      setAvailablePermissions(
        [...permissions].sort(
          (left, right) =>
            left.code.localeCompare(
              right.code,
            ),
        ),
      )

      setAssignedPermissions(
        [...assignments].sort(
          (left, right) =>
            left.permissionCode.localeCompare(
              right.permissionCode,
            ),
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить разрешения роли',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  const assignedPermissionIds =
    useMemo(
      () =>
        new Set(
          assignedPermissions.map(
            (assignment) =>
              assignment.permissionId,
          ),
        ),
      [assignedPermissions],
    )

  const grantablePermissions =
    useMemo(
      () =>
        availablePermissions.filter(
          (permission) =>
            !assignedPermissionIds.has(
              permission.id,
            ),
        ),
      [
        availablePermissions,
        assignedPermissionIds,
      ],
    )

  const canModify =
    !role.systemRole

  const canGrant =
    canModify && role.active

  async function handleGrant(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (!canGrant) {
      return
    }

    if (!selectedPermissionId) {
      setError(
        'Выберите разрешение для назначения',
      )
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsGranting(true)
    setError(null)

    try {
      const response = await fetch(
        `/api/roles/${role.id}/permissions`,
        {
          method: 'POST',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify({
            permissionId:
              selectedPermissionId,
          }),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для назначения этого разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось назначить разрешение: HTTP ${response.status}`,
          ),
        )
      }

      const assignment:
        RolePermissionResponse =
          await response.json()

      setAssignedPermissions(
        (current) =>
          [
            ...current,
            assignment,
          ].sort(
            (left, right) =>
              left.permissionCode.localeCompare(
                right.permissionCode,
              ),
          ),
      )

      setSelectedPermissionId('')
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось назначить разрешение',
        ),
      )
    } finally {
      setIsGranting(false)
    }
  }

  async function handleRevoke(
    assignment:
      RolePermissionResponse,
  ) {
    if (!canModify) {
      return
    }

    const confirmed =
      window.confirm(
        `Отозвать разрешение «${assignment.permissionName}» (${assignment.permissionCode}) у роли ${role.code}?`,
      )

    if (!confirmed) {
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setRevokingPermissionId(
      assignment.permissionId,
    )

    setError(null)

    try {
      const response = await fetch(
        `/api/roles/${role.id}/permissions/${assignment.permissionId}`,
        {
          method: 'DELETE',

          headers: {
            Authorization:
              `Bearer ${accessToken}`,
          },
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для отзыва этого разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось отозвать разрешение: HTTP ${response.status}`,
          ),
        )
      }

      setAssignedPermissions(
        (current) =>
          current.filter(
            (item) =>
              item.permissionId !==
              assignment.permissionId,
          ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось отозвать разрешение',
        ),
      )
    } finally {
      setRevokingPermissionId(null)
    }
  }

  const isProcessing =
    isGranting ||
    revokingPermissionId !== null

  return (
    <div className="modal-backdrop">
      <section
        className="create-user-modal user-roles-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="role-permissions-title"
      >
        <div className="modal-header">
          <div>
            <p className="dashboard-eyebrow">
              RBAC PERMISSIONS
            </p>

            <h2 id="role-permissions-title">
              Разрешения роли
            </h2>

            <p>
              {role.code}
              {' · '}
              {role.name}
            </p>
          </div>

          <button
            className="modal-close"
            type="button"
            onClick={onClose}
            disabled={isProcessing}
            aria-label="Закрыть"
          >
            ×
          </button>
        </div>

        {role.systemRole && (
          <div className="role-assignment-warning">
            Это системная роль.
            Её разрешения доступны только
            для просмотра и не могут быть
            изменены.
          </div>
        )}

        {!role.systemRole &&
          !role.active && (
            <div className="role-assignment-warning">
              Роль неактивна. Новые
              разрешения назначать нельзя,
              но существующие назначения
              можно просматривать и
              отзывать.
            </div>
          )}

        {error && (
          <div
            className="login-error"
            role="alert"
          >
            {error}
          </div>
        )}

        {isLoading ? (
          <div className="users-state">
            Загрузка разрешений...
          </div>
        ) : (
          <>
            <section className="user-role-section">
              <div className="user-role-section-heading">
                <div>
                  <h3>
                    Назначенные разрешения
                  </h3>

                  <span>
                    {
                      assignedPermissions.length
                    }
                  </span>
                </div>
              </div>

              {assignedPermissions.length ===
              0 ? (
                <div className="user-role-empty">
                  У роли пока нет
                  назначенных разрешений.
                </div>
              ) : (
                <div className="assigned-role-list">
                  {assignedPermissions.map(
                    (assignment) => (
                      <article
                        className="assigned-role-card"
                        key={
                          assignment.permissionId
                        }
                      >
                        <div className="assigned-role-main">
                          <span className="department-code">
                            {
                              assignment.permissionCode
                            }
                          </span>

                          <div>
                            <strong>
                              {
                                assignment.permissionName
                              }
                            </strong>

                            <span>
                              Назначено:{' '}
                              {formatDateTime(
                                assignment.grantedAt,
                              )}
                            </span>
                          </div>
                        </div>

                        <button
                          className="table-action table-action-danger"
                          type="button"
                          disabled={
                            !canModify ||
                            revokingPermissionId ===
                              assignment.permissionId
                          }
                          onClick={() => {
                            void handleRevoke(
                              assignment,
                            )
                          }}
                        >
                          {revokingPermissionId ===
                          assignment.permissionId
                            ? 'Отзыв...'
                            : 'Отозвать'}
                        </button>
                      </article>
                    ),
                  )}
                </div>
              )}
            </section>

            <section className="user-role-section">
              <div className="user-role-section-heading">
                <div>
                  <h3>
                    Назначить разрешение
                  </h3>

                  <span>
                    {
                      grantablePermissions.length
                    }{' '}
                    доступно
                  </span>
                </div>
              </div>

              <form
                className="role-assignment-form"
                onSubmit={handleGrant}
              >
                <label>
                  Разрешение

                  <select
                    value={
                      selectedPermissionId
                    }
                    onChange={(event) =>
                      setSelectedPermissionId(
                        event.target.value,
                      )
                    }
                    disabled={
                      !canGrant ||
                      isGranting ||
                      grantablePermissions.length ===
                        0
                    }
                    required
                  >
                    <option value="">
                      {grantablePermissions.length ===
                      0
                        ? 'Все доступные разрешения уже назначены'
                        : 'Выберите разрешение'}
                    </option>

                    {grantablePermissions.map(
                      (permission) => (
                        <option
                          key={
                            permission.id
                          }
                          value={
                            permission.id
                          }
                        >
                          {permission.code}
                          {' — '}
                          {permission.name}
                        </option>
                      ),
                    )}
                  </select>
                </label>

                <button
                  className="primary-action role-assignment-submit"
                  type="submit"
                  disabled={
                    !canGrant ||
                    isGranting ||
                    !selectedPermissionId
                  }
                >
                  {isGranting
                    ? 'Назначение...'
                    : 'Назначить разрешение'}
                </button>
              </form>
            </section>
          </>
        )}

        <div className="modal-actions">
          <button
            className="secondary-action"
            type="button"
            onClick={onClose}
            disabled={isProcessing}
          >
            Закрыть
          </button>
        </div>
      </section>
    </div>
  )
}

function getAccessToken() {
  return sessionStorage.getItem(
    'accessToken',
  )
}

function redirectToLogin() {
  sessionStorage.clear()
  window.location.href = '/'
}

function formatDateTime(
  value: string,
) {
  return new Intl.DateTimeFormat(
    'ru-RU',
    {
      dateStyle: 'short',
      timeStyle: 'short',
    },
  ).format(new Date(value))
}

function getErrorMessage(
  exception: unknown,
  fallback: string,
) {
  if (exception instanceof Error) {
    return exception.message
  }

  return fallback
}

async function readApiError(
  response: Response,
  fallback: string,
) {
  try {
    const body: ApiErrorResponse =
      await response.json()

    if (body.fieldErrors) {
      const messages =
        Object.values(
          body.fieldErrors,
        )

      if (messages.length > 0) {
        return messages.join('. ')
      }
    }

    if (
      body.message &&
      body.message.trim()
    ) {
      return body.message
    }

    return fallback
  } catch {
    return fallback
  }
}

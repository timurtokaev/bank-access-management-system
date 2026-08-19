import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
} from 'react'

type UserStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'LOCKED'

type UserSummary = {
  id: string
  username: string
  firstName: string
  lastName: string
  status: UserStatus
}

type RoleResponse = {
  id: string
  code: string
  name: string
  description: string | null
  systemRole: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}

type UserRoleResponse = {
  userId: string
  employeeNumber: string
  username: string
  roleId: string
  roleCode: string
  roleName: string
  assignedById: string | null
  assignedByUsername: string | null
  assignedAt: string
  expiresAt: string | null
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

type UserRolesModalProps = {
  user: UserSummary
  onClose: () => void
}

export default function UserRolesModal({
  user,
  onClose,
}: UserRolesModalProps) {
  const [availableRoles, setAvailableRoles] =
    useState<RoleResponse[]>([])

  const [assignedRoles, setAssignedRoles] =
    useState<UserRoleResponse[]>([])

  const [selectedRoleId, setSelectedRoleId] =
    useState('')

  const [expiresAt, setExpiresAt] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [isAssigning, setIsAssigning] =
    useState(false)

  const [
    revokingRoleId,
    setRevokingRoleId,
  ] = useState<string | null>(null)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    void loadData()
  }, [user.id])

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
        rolesResponse,
        assignedResponse,
      ] = await Promise.all([
        fetch(
          '/api/roles/active',
          {
            headers: {
              Authorization:
                `Bearer ${accessToken}`,
            },
          },
        ),

        fetch(
          `/api/users/${user.id}/roles`,
          {
            headers: {
              Authorization:
                `Bearer ${accessToken}`,
            },
          },
        ),
      ])

      if (
        rolesResponse.status === 401 ||
        assignedResponse.status === 401
      ) {
        redirectToLogin()
        return
      }

      if (
        rolesResponse.status === 403 ||
        assignedResponse.status === 403
      ) {
        throw new Error(
          'Недостаточно прав для просмотра назначений ролей',
        )
      }

      if (!rolesResponse.ok) {
        throw new Error(
          await readApiError(
            rolesResponse,
            `Не удалось загрузить роли: HTTP ${rolesResponse.status}`,
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

      const roles: RoleResponse[] =
        await rolesResponse.json()

      const assignments: UserRoleResponse[] =
        await assignedResponse.json()

      setAvailableRoles(
        [...roles].sort(
          (left, right) =>
            left.code.localeCompare(
              right.code,
            ),
        ),
      )

      setAssignedRoles(
        [...assignments].sort(
          (left, right) =>
            left.roleCode.localeCompare(
              right.roleCode,
            ),
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить роли пользователя',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  const assignedRoleIds =
    useMemo(
      () =>
        new Set(
          assignedRoles.map(
            (assignment) =>
              assignment.roleId,
          ),
        ),
      [assignedRoles],
    )

  const assignableRoles =
    useMemo(
      () =>
        availableRoles.filter(
          (role) =>
            !assignedRoleIds.has(
              role.id,
            ),
        ),
      [
        availableRoles,
        assignedRoleIds,
      ],
    )

  async function handleAssign(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (!selectedRoleId) {
      setError(
        'Выберите роль для назначения',
      )
      return
    }

    if (user.status !== 'ACTIVE') {
      setError(
        'Назначать роли можно только активному пользователю',
      )
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsAssigning(true)
    setError(null)

    try {
      const normalizedExpiration =
        expiresAt
          ? new Date(
              expiresAt,
            ).toISOString()
          : null

      const response = await fetch(
        `/api/users/${user.id}/roles`,
        {
          method: 'POST',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify({
            roleId:
              selectedRoleId,

            expiresAt:
              normalizedExpiration,
          }),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для назначения этой роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось назначить роль: HTTP ${response.status}`,
          ),
        )
      }

      const assignment: UserRoleResponse =
        await response.json()

      setAssignedRoles((current) =>
        [
          ...current,
          assignment,
        ].sort(
          (left, right) =>
            left.roleCode.localeCompare(
              right.roleCode,
            ),
        ),
      )

      setSelectedRoleId('')
      setExpiresAt('')
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось назначить роль',
        ),
      )
    } finally {
      setIsAssigning(false)
    }
  }

  async function handleRevoke(
    assignment: UserRoleResponse,
  ) {
    const confirmed =
      window.confirm(
        `Отозвать роль «${assignment.roleName}» (${assignment.roleCode}) у пользователя ${user.username}?`,
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

    setRevokingRoleId(
      assignment.roleId,
    )

    setError(null)

    try {
      const response = await fetch(
        `/api/users/${user.id}/roles/${assignment.roleId}`,
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
          'Недостаточно прав для отзыва этой роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось отозвать роль: HTTP ${response.status}`,
          ),
        )
      }

      setAssignedRoles(
        (current) =>
          current.filter(
            (item) =>
              item.roleId !==
              assignment.roleId,
          ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось отозвать роль',
        ),
      )
    } finally {
      setRevokingRoleId(null)
    }
  }

  const isUserActive =
    user.status === 'ACTIVE'

  return (
    <div className="modal-backdrop">
      <section
        className="create-user-modal user-roles-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-roles-title"
      >
        <div className="modal-header">
          <div>
            <p className="dashboard-eyebrow">
              RBAC ASSIGNMENTS
            </p>

            <h2 id="user-roles-title">
              Роли пользователя
            </h2>

            <p>
              {user.firstName}{' '}
              {user.lastName}
              {' · '}
              {user.username}
            </p>
          </div>

          <button
            className="modal-close"
            type="button"
            onClick={onClose}
            disabled={
              isAssigning ||
              revokingRoleId !== null
            }
            aria-label="Закрыть"
          >
            ×
          </button>
        </div>

        {!isUserActive && (
          <div className="role-assignment-warning">
            Пользователь имеет статус{' '}
            <strong>
              {formatStatus(
                user.status,
              )}
            </strong>
            . Новые роли назначать нельзя,
            но текущие назначения можно
            просматривать.
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
            Загрузка ролей...
          </div>
        ) : (
          <>
            <section className="user-role-section">
              <div className="user-role-section-heading">
                <div>
                  <h3>
                    Назначенные роли
                  </h3>

                  <span>
                    {
                      assignedRoles.length
                    }
                  </span>
                </div>
              </div>

              {assignedRoles.length ===
              0 ? (
                <div className="user-role-empty">
                  У пользователя пока нет
                  назначенных ролей.
                </div>
              ) : (
                <div className="assigned-role-list">
                  {assignedRoles.map(
                    (assignment) => (
                      <article
                        className="assigned-role-card"
                        key={
                          assignment.roleId
                        }
                      >
                        <div className="assigned-role-main">
                          <span className="department-code">
                            {
                              assignment.roleCode
                            }
                          </span>

                          <div>
                            <strong>
                              {
                                assignment.roleName
                              }
                            </strong>

                            <span>
                              Назначил:{' '}
                              {assignment.assignedByUsername ??
                                'system'}
                            </span>

                            <span>
                              Назначена:{' '}
                              {formatDateTime(
                                assignment.assignedAt,
                              )}
                            </span>

                            <span>
                              Срок:{' '}
                              {assignment.expiresAt
                                ? formatDateTime(
                                    assignment.expiresAt,
                                  )
                                : 'без ограничения'}
                            </span>
                          </div>
                        </div>

                        <button
                          className="table-action table-action-danger"
                          type="button"
                          disabled={
                            revokingRoleId ===
                            assignment.roleId
                          }
                          onClick={() => {
                            void handleRevoke(
                              assignment,
                            )
                          }}
                        >
                          {revokingRoleId ===
                          assignment.roleId
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
                    Назначить роль
                  </h3>

                  <span>
                    {
                      assignableRoles.length
                    }{' '}
                    доступно
                  </span>
                </div>
              </div>

              <form
                className="role-assignment-form"
                onSubmit={handleAssign}
              >
                <label>
                  Роль

                  <select
                    value={
                      selectedRoleId
                    }
                    onChange={(event) =>
                      setSelectedRoleId(
                        event.target.value,
                      )
                    }
                    disabled={
                      !isUserActive ||
                      isAssigning ||
                      assignableRoles.length ===
                        0
                    }
                    required
                  >
                    <option value="">
                      {assignableRoles.length ===
                      0
                        ? 'Все доступные роли уже назначены'
                        : 'Выберите роль'}
                    </option>

                    {assignableRoles.map(
                      (role) => (
                        <option
                          key={role.id}
                          value={role.id}
                        >
                          {role.code}
                          {' — '}
                          {role.name}
                        </option>
                      ),
                    )}
                  </select>
                </label>

                <label>
                  Действует до
                  <span className="field-optional">
                    необязательно
                  </span>

                  <input
                    type="datetime-local"
                    value={expiresAt}
                    onChange={(event) =>
                      setExpiresAt(
                        event.target.value,
                      )
                    }
                    min={getMinimumDateTime()}
                    disabled={
                      !isUserActive ||
                      isAssigning
                    }
                  />
                </label>

                <button
                  className="primary-action role-assignment-submit"
                  type="submit"
                  disabled={
                    !isUserActive ||
                    isAssigning ||
                    !selectedRoleId
                  }
                >
                  {isAssigning
                    ? 'Назначение...'
                    : 'Назначить роль'}
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
            disabled={
              isAssigning ||
              revokingRoleId !== null
            }
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

function getMinimumDateTime() {
  const date =
    new Date(
      Date.now() + 60_000,
    )

  const offset =
    date.getTimezoneOffset()

  const localDate =
    new Date(
      date.getTime() -
        offset * 60_000,
    )

  return localDate
    .toISOString()
    .slice(0, 16)
}

function formatStatus(
  status: UserStatus,
) {
  if (status === 'ACTIVE') {
    return 'Активен'
  }

  if (status === 'LOCKED') {
    return 'Заблокирован'
  }

  return 'Неактивен'
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
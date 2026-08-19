import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
  ReactNode,
} from 'react'

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

type CreatePermissionForm = {
  code: string
  name: string
  description: string
}

type EditPermissionForm = {
  name: string
  description: string
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

type PermissionsPageProps = {
  layout: (children: ReactNode) => ReactNode
}

const emptyCreatePermissionForm:
  CreatePermissionForm = {
    code: '',
    name: '',
    description: '',
  }

export default function PermissionsPage({
  layout,
}: PermissionsPageProps) {
  const [
    permissions,
    setPermissions,
  ] = useState<PermissionResponse[]>([])

  const [search, setSearch] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [error, setError] =
    useState<string | null>(null)

  const [
    isCreateOpen,
    setIsCreateOpen,
  ] = useState(false)

  const [
    createForm,
    setCreateForm,
  ] = useState<CreatePermissionForm>(
    emptyCreatePermissionForm,
  )

  const [isCreating, setIsCreating] =
    useState(false)

  const [
    createError,
    setCreateError,
  ] = useState<string | null>(null)

  const [
    editingPermission,
    setEditingPermission,
  ] =
    useState<PermissionResponse | null>(
      null,
    )

  const [editForm, setEditForm] =
    useState<EditPermissionForm | null>(
      null,
    )

  const [isUpdating, setIsUpdating] =
    useState(false)

  const [editError, setEditError] =
    useState<string | null>(null)

  const [
    processingPermissionId,
    setProcessingPermissionId,
  ] = useState<string | null>(null)

  useEffect(() => {
    void loadPermissions()
  }, [])

  async function loadPermissions() {
    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const response = await fetch(
        '/api/permissions',
        {
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
          'Недостаточно прав для просмотра разрешений',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось загрузить разрешения: HTTP ${response.status}`,
          ),
        )
      }

      const data: PermissionResponse[] =
        await response.json()

      setPermissions(
        sortPermissions(data),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить разрешения',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  function openCreateModal() {
    setCreateError(null)

    setCreateForm(
      emptyCreatePermissionForm,
    )

    setIsCreateOpen(true)
  }

  function closeCreateModal() {
    if (isCreating) {
      return
    }

    setIsCreateOpen(false)
    setCreateError(null)

    setCreateForm(
      emptyCreatePermissionForm,
    )
  }

  function updateCreateField(
    field: keyof CreatePermissionForm,
    value: string,
  ) {
    setCreateForm((current) => ({
      ...current,

      [field]:
        field === 'code'
          ? value.toUpperCase()
          : value,
    }))
  }

  async function handleCreatePermission(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsCreating(true)
    setCreateError(null)

    try {
      const response = await fetch(
        '/api/permissions',
        {
          method: 'POST',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify({
            code: createForm.code,
            name: createForm.name,

            description:
              createForm.description.trim() ||
              null,
          }),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для создания разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось создать разрешение: HTTP ${response.status}`,
          ),
        )
      }

      const createdPermission:
        PermissionResponse =
        await response.json()

      setPermissions((current) =>
        sortPermissions([
          ...current,
          createdPermission,
        ]),
      )

      setIsCreateOpen(false)

      setCreateForm(
        emptyCreatePermissionForm,
      )
    } catch (exception) {
      setCreateError(
        getErrorMessage(
          exception,
          'Не удалось создать разрешение',
        ),
      )
    } finally {
      setIsCreating(false)
    }
  }

  function openEditModal(
    permission: PermissionResponse,
  ) {
    if (
      permission.systemPermission
    ) {
      return
    }

    setEditError(null)

    setEditingPermission(
      permission,
    )

    setEditForm({
      name: permission.name,

      description:
        permission.description ?? '',
    })
  }

  function closeEditModal() {
    if (isUpdating) {
      return
    }

    setEditingPermission(null)
    setEditForm(null)
    setEditError(null)
  }

  function updateEditField(
    field: keyof EditPermissionForm,
    value: string,
  ) {
    setEditForm((current) => {
      if (!current) {
        return current
      }

      return {
        ...current,
        [field]: value,
      }
    })
  }

  async function handleUpdatePermission(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (
      !editingPermission ||
      !editForm
    ) {
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsUpdating(true)
    setEditError(null)

    try {
      const response = await fetch(
        `/api/permissions/${editingPermission.id}`,
        {
          method: 'PUT',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify({
            name: editForm.name,

            description:
              editForm.description.trim() ||
              null,
          }),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для редактирования разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось обновить разрешение: HTTP ${response.status}`,
          ),
        )
      }

      const updatedPermission:
        PermissionResponse =
        await response.json()

      setPermissions((current) =>
        sortPermissions(
          current.map(
            (permission) =>
              permission.id ===
              updatedPermission.id
                ? updatedPermission
                : permission,
          ),
        ),
      )

      setEditingPermission(null)
      setEditForm(null)
    } catch (exception) {
      setEditError(
        getErrorMessage(
          exception,
          'Не удалось обновить разрешение',
        ),
      )
    } finally {
      setIsUpdating(false)
    }
  }

  async function handleDeactivate(
    permission: PermissionResponse,
  ) {
    if (
      permission.systemPermission ||
      !permission.active
    ) {
      return
    }

    const confirmed =
      window.confirm(
        `Деактивировать разрешение «${permission.name}» (${permission.code})?`,
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

    setProcessingPermissionId(
      permission.id,
    )

    setError(null)

    try {
      const response = await fetch(
        `/api/permissions/${permission.id}`,
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
          'Недостаточно прав для деактивации разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось деактивировать разрешение: HTTP ${response.status}`,
          ),
        )
      }

      setPermissions((current) =>
        current.map((item) =>
          item.id === permission.id
            ? {
                ...item,
                active: false,
              }
            : item,
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось деактивировать разрешение',
        ),
      )
    } finally {
      setProcessingPermissionId(
        null,
      )
    }
  }

  async function handleActivate(
    permission: PermissionResponse,
  ) {
    if (permission.active) {
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setProcessingPermissionId(
      permission.id,
    )

    setError(null)

    try {
      const response = await fetch(
        `/api/permissions/${permission.id}/activate`,
        {
          method: 'PUT',

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
          'Недостаточно прав для активации разрешения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось активировать разрешение: HTTP ${response.status}`,
          ),
        )
      }

      const activatedPermission:
        PermissionResponse =
        await response.json()

      setPermissions((current) =>
        sortPermissions(
          current.map((item) =>
            item.id ===
            activatedPermission.id
              ? activatedPermission
              : item,
          ),
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось активировать разрешение',
        ),
      )
    } finally {
      setProcessingPermissionId(
        null,
      )
    }
  }

  const filteredPermissions =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase()

      if (!query) {
        return permissions
      }

      return permissions.filter(
        (permission) => {
          const searchable = [
            permission.code,
            permission.name,
            permission.description ?? '',

            permission.systemPermission
              ? 'системное'
              : 'пользовательское',

            permission.active
              ? 'активно'
              : 'неактивно',
          ]
            .join(' ')
            .toLowerCase()

          return searchable.includes(
            query,
          )
        },
      )
    }, [permissions, search])

  return layout(
    <>
      <section className="users-toolbar">
        <div className="users-search">
          <input
            type="search"
            value={search}
            onChange={(event) =>
              setSearch(
                event.target.value,
              )
            }
            placeholder="Поиск по коду, названию или описанию..."
          />
        </div>

        <button
          className="primary-action"
          type="button"
          onClick={
            openCreateModal
          }
        >
          + Добавить разрешение
        </button>
      </section>

      <section className="users-panel roles-panel">
        <div className="users-panel-header">
          <div>
            <h2>
              Разрешения доступа
            </h2>

            <p>
              Всего:{' '}
              {
                filteredPermissions.length
              }
            </p>
          </div>

          <button
            className="refresh-button"
            type="button"
            onClick={() => {
              void loadPermissions()
            }}
            disabled={isLoading}
          >
            Обновить
          </button>
        </div>

        {error && (
          <div
            className="login-error"
            role="alert"
          >
            {error}
          </div>
        )}

        {isLoading && (
          <div className="users-state">
            Загрузка разрешений...
          </div>
        )}

        {!isLoading &&
          filteredPermissions.length ===
            0 && (
            <div className="users-state">
              Разрешения не найдены
            </div>
          )}

        {!isLoading &&
          filteredPermissions.length >
            0 && (
            <div className="users-table-wrapper">
              <table className="users-table roles-table">
                <thead>
                  <tr>
                    <th>Код</th>
                    <th>Разрешение</th>
                    <th>Тип</th>
                    <th>Статус</th>
                    <th>Описание</th>
                    <th>Действия</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredPermissions.map(
                    (permission) => {
                      const isProcessing =
                        processingPermissionId ===
                        permission.id

                      return (
                        <tr
                          key={
                            permission.id
                          }
                        >
                          <td>
                            <span className="department-code">
                              {
                                permission.code
                              }
                            </span>
                          </td>

                          <td>
                            <div className="role-name-cell">
                              <div className="role-icon">
                                {permission.name
                                  .charAt(0)
                                  .toUpperCase()}
                              </div>

                              <div>
                                <strong>
                                  {
                                    permission.name
                                  }
                                </strong>

                                <span>
                                  {
                                    permission.id
                                  }
                                </span>
                              </div>
                            </div>
                          </td>

                          <td>
                            <span
                              className={
                                permission.systemPermission
                                  ? 'role-type-badge role-type-system'
                                  : 'role-type-badge'
                              }
                            >
                              {permission.systemPermission
                                ? 'Системное'
                                : 'Пользовательское'}
                            </span>
                          </td>

                          <td>
                            <span
                              className={
                                permission.active
                                  ? 'user-status user-status-active'
                                  : 'user-status user-status-inactive'
                              }
                            >
                              {permission.active
                                ? 'Активно'
                                : 'Неактивно'}
                            </span>
                          </td>

                          <td>
                            <div className="role-description">
                              {permission.description ??
                                '—'}
                            </div>
                          </td>

                          <td>
                            <div className="user-actions role-actions">
                              <button
                                className="table-action"
                                type="button"
                                disabled={
                                  permission.systemPermission ||
                                  isProcessing
                                }
                                onClick={() =>
                                  openEditModal(
                                    permission,
                                  )
                                }
                                title={
                                  permission.systemPermission
                                    ? 'Системное разрешение нельзя редактировать'
                                    : undefined
                                }
                              >
                                Редактировать
                              </button>

                              {permission.active ? (
                                <button
                                  className="table-action table-action-danger"
                                  type="button"
                                  disabled={
                                    permission.systemPermission ||
                                    isProcessing
                                  }
                                  onClick={() => {
                                    void handleDeactivate(
                                      permission,
                                    )
                                  }}
                                  title={
                                    permission.systemPermission
                                      ? 'Системное разрешение нельзя деактивировать'
                                      : undefined
                                  }
                                >
                                  {isProcessing
                                    ? 'Обработка...'
                                    : 'Деактивировать'}
                                </button>
                              ) : (
                                <button
                                  className="table-action"
                                  type="button"
                                  disabled={
                                    isProcessing
                                  }
                                  onClick={() => {
                                    void handleActivate(
                                      permission,
                                    )
                                  }}
                                >
                                  {isProcessing
                                    ? 'Обработка...'
                                    : 'Активировать'}
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      )
                    },
                  )}
                </tbody>
              </table>
            </div>
          )}
      </section>

      {isCreateOpen && (
        <div className="modal-backdrop">
          <section
            className="create-user-modal role-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-permission-title"
          >
            <div className="modal-header">
              <div>
                <p className="dashboard-eyebrow">
                  RBAC
                </p>

                <h2 id="create-permission-title">
                  Новое разрешение
                </h2>

                <p>
                  Создание разрешения для
                  модели разграничения
                  доступа.
                </p>
              </div>

              <button
                className="modal-close"
                type="button"
                onClick={
                  closeCreateModal
                }
                disabled={
                  isCreating
                }
                aria-label="Закрыть"
              >
                ×
              </button>
            </div>

            <form
              className="create-user-form"
              onSubmit={
                handleCreatePermission
              }
            >
              <div className="form-grid">
                <label className="form-field-wide">
                  Код разрешения

                  <input
                    type="text"
                    value={
                      createForm.code
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'code',
                        event.target.value,
                      )
                    }
                    placeholder="REPORT_EXPORT"
                    maxLength={100}
                    pattern="[A-Z][A-Z0-9_]*"
                    required
                  />
                </label>

                <label className="form-field-wide">
                  Название

                  <input
                    type="text"
                    value={
                      createForm.name
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'name',
                        event.target.value,
                      )
                    }
                    placeholder="Экспорт отчётов"
                    maxLength={150}
                    required
                  />
                </label>

                <label className="form-field-wide">
                  Описание

                  <textarea
                    value={
                      createForm.description
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'description',
                        event.target.value,
                      )
                    }
                    placeholder="Опишите действие, которое разрешает это право..."
                    maxLength={1000}
                    rows={4}
                  />
                </label>
              </div>

              {createError && (
                <div
                  className="login-error"
                  role="alert"
                >
                  {createError}
                </div>
              )}

              <div className="modal-actions">
                <button
                  className="secondary-action"
                  type="button"
                  onClick={
                    closeCreateModal
                  }
                  disabled={
                    isCreating
                  }
                >
                  Отмена
                </button>

                <button
                  className="primary-action"
                  type="submit"
                  disabled={
                    isCreating
                  }
                >
                  {isCreating
                    ? 'Создание...'
                    : 'Создать разрешение'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

      {editingPermission &&
        editForm && (
          <div className="modal-backdrop">
            <section
              className="create-user-modal role-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="edit-permission-title"
            >
              <div className="modal-header">
                <div>
                  <p className="dashboard-eyebrow">
                    RBAC
                  </p>

                  <h2 id="edit-permission-title">
                    Редактирование разрешения
                  </h2>

                  <p>
                    Изменение названия и
                    описания разрешения.
                  </p>
                </div>

                <button
                  className="modal-close"
                  type="button"
                  onClick={
                    closeEditModal
                  }
                  disabled={
                    isUpdating
                  }
                  aria-label="Закрыть"
                >
                  ×
                </button>
              </div>

              <div className="modal-info">
                <strong>
                  {
                    editingPermission.code
                  }
                </strong>

                <span>
                  Код разрешения изменить
                  нельзя
                </span>
              </div>

              <form
                className="create-user-form"
                onSubmit={
                  handleUpdatePermission
                }
              >
                <div className="form-grid">
                  <label className="form-field-wide">
                    Название

                    <input
                      type="text"
                      value={
                        editForm.name
                      }
                      onChange={(event) =>
                        updateEditField(
                          'name',
                          event.target.value,
                        )
                      }
                      maxLength={150}
                      required
                    />
                  </label>

                  <label className="form-field-wide">
                    Описание

                    <textarea
                      value={
                        editForm.description
                      }
                      onChange={(event) =>
                        updateEditField(
                          'description',
                          event.target.value,
                        )
                      }
                      maxLength={1000}
                      rows={4}
                    />
                  </label>
                </div>

                {editError && (
                  <div
                    className="login-error"
                    role="alert"
                  >
                    {editError}
                  </div>
                )}

                <div className="modal-actions">
                  <button
                    className="secondary-action"
                    type="button"
                    onClick={
                      closeEditModal
                    }
                    disabled={
                      isUpdating
                    }
                  >
                    Отмена
                  </button>

                  <button
                    className="primary-action"
                    type="submit"
                    disabled={
                      isUpdating
                    }
                  >
                    {isUpdating
                      ? 'Сохранение...'
                      : 'Сохранить изменения'}
                  </button>
                </div>
              </form>
            </section>
          </div>
        )}
    </>,
  )
}

function sortPermissions(
  permissions: PermissionResponse[],
) {
  return [...permissions].sort(
    (left, right) =>
      left.code.localeCompare(
        right.code,
      ),
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
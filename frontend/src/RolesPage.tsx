import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
  ReactNode,
} from 'react'

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

type CreateRoleForm = {
  code: string
  name: string
  description: string
}

type EditRoleForm = {
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

type RolesPageProps = {
  layout: (children: ReactNode) => ReactNode
}

const emptyCreateRoleForm: CreateRoleForm = {
  code: '',
  name: '',
  description: '',
}

export default function RolesPage({
  layout,
}: RolesPageProps) {
  const [roles, setRoles] =
    useState<RoleResponse[]>([])

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

  const [createForm, setCreateForm] =
    useState<CreateRoleForm>(
      emptyCreateRoleForm,
    )

  const [isCreating, setIsCreating] =
    useState(false)

  const [
    createError,
    setCreateError,
  ] = useState<string | null>(null)

  const [editingRole, setEditingRole] =
    useState<RoleResponse | null>(null)

  const [editForm, setEditForm] =
    useState<EditRoleForm | null>(null)

  const [isUpdating, setIsUpdating] =
    useState(false)

  const [editError, setEditError] =
    useState<string | null>(null)

  const [
    processingRoleId,
    setProcessingRoleId,
  ] = useState<string | null>(null)

  useEffect(() => {
    void loadRoles()
  }, [])

  async function loadRoles() {
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
        '/api/roles',
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
          'Недостаточно прав для просмотра ролей',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось загрузить роли: HTTP ${response.status}`,
          ),
        )
      }

      const data: RoleResponse[] =
        await response.json()

      setRoles(sortRoles(data))
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить роли',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  function openCreateModal() {
    setCreateError(null)

    setCreateForm(
      emptyCreateRoleForm,
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
      emptyCreateRoleForm,
    )
  }

  function updateCreateField(
    field: keyof CreateRoleForm,
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

  async function handleCreateRole(
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
        '/api/roles',
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
          'Недостаточно прав для создания роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось создать роль: HTTP ${response.status}`,
          ),
        )
      }

      const createdRole: RoleResponse =
        await response.json()

      setRoles((current) =>
        sortRoles([
          ...current,
          createdRole,
        ]),
      )

      setIsCreateOpen(false)

      setCreateForm(
        emptyCreateRoleForm,
      )
    } catch (exception) {
      setCreateError(
        getErrorMessage(
          exception,
          'Не удалось создать роль',
        ),
      )
    } finally {
      setIsCreating(false)
    }
  }

  function openEditModal(
    role: RoleResponse,
  ) {
    if (role.systemRole) {
      return
    }

    setEditError(null)

    setEditingRole(role)

    setEditForm({
      name: role.name,
      description:
        role.description ?? '',
    })
  }

  function closeEditModal() {
    if (isUpdating) {
      return
    }

    setEditingRole(null)
    setEditForm(null)
    setEditError(null)
  }

  function updateEditField(
    field: keyof EditRoleForm,
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

  async function handleUpdateRole(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (!editingRole || !editForm) {
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
        `/api/roles/${editingRole.id}`,
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
          'Недостаточно прав для редактирования роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось обновить роль: HTTP ${response.status}`,
          ),
        )
      }

      const updatedRole: RoleResponse =
        await response.json()

      setRoles((current) =>
        sortRoles(
          current.map((role) =>
            role.id === updatedRole.id
              ? updatedRole
              : role,
          ),
        ),
      )

      setEditingRole(null)
      setEditForm(null)
    } catch (exception) {
      setEditError(
        getErrorMessage(
          exception,
          'Не удалось обновить роль',
        ),
      )
    } finally {
      setIsUpdating(false)
    }
  }

  async function handleDeactivate(
    role: RoleResponse,
  ) {
    if (
      role.systemRole ||
      !role.active
    ) {
      return
    }

    const confirmed =
      window.confirm(
        `Деактивировать роль «${role.name}» (${role.code})?`,
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

    setProcessingRoleId(role.id)
    setError(null)

    try {
      const response = await fetch(
        `/api/roles/${role.id}`,
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
          'Недостаточно прав для деактивации роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось деактивировать роль: HTTP ${response.status}`,
          ),
        )
      }

      setRoles((current) =>
        current.map((item) =>
          item.id === role.id
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
          'Не удалось деактивировать роль',
        ),
      )
    } finally {
      setProcessingRoleId(null)
    }
  }

  async function handleActivate(
    role: RoleResponse,
  ) {
    if (role.active) {
      return
    }

    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setProcessingRoleId(role.id)
    setError(null)

    try {
      const response = await fetch(
        `/api/roles/${role.id}/activate`,
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
          'Недостаточно прав для активации роли',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось активировать роль: HTTP ${response.status}`,
          ),
        )
      }

      const activatedRole: RoleResponse =
        await response.json()

      setRoles((current) =>
        sortRoles(
          current.map((item) =>
            item.id ===
            activatedRole.id
              ? activatedRole
              : item,
          ),
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось активировать роль',
        ),
      )
    } finally {
      setProcessingRoleId(null)
    }
  }

  const filteredRoles =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase()

      if (!query) {
        return roles
      }

      return roles.filter((role) => {
        const searchable = [
          role.code,
          role.name,
          role.description ?? '',
          role.systemRole
            ? 'системная'
            : 'пользовательская',
          role.active
            ? 'активна'
            : 'неактивна',
        ]
          .join(' ')
          .toLowerCase()

        return searchable.includes(
          query,
        )
      })
    }, [roles, search])

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
          + Добавить роль
        </button>
      </section>

      <section className="users-panel roles-panel">
        <div className="users-panel-header">
          <div>
            <h2>
              Роли доступа
            </h2>

            <p>
              Всего: {
                filteredRoles.length
              }
            </p>
          </div>

          <button
            className="refresh-button"
            type="button"
            onClick={() => {
              void loadRoles()
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
            Загрузка ролей...
          </div>
        )}

        {!isLoading &&
          filteredRoles.length ===
            0 && (
            <div className="users-state">
              Роли не найдены
            </div>
          )}

        {!isLoading &&
          filteredRoles.length >
            0 && (
            <div className="users-table-wrapper">
              <table className="users-table roles-table">
                <thead>
                  <tr>
                    <th>Код</th>
                    <th>Роль</th>
                    <th>Тип</th>
                    <th>Статус</th>
                    <th>Описание</th>
                    <th>Действия</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredRoles.map(
                    (role) => {
                      const isProcessing =
                        processingRoleId ===
                        role.id

                      return (
                        <tr key={role.id}>
                          <td>
                            <span className="department-code">
                              {role.code}
                            </span>
                          </td>

                          <td>
                            <div className="role-name-cell">
                              <div className="role-icon">
                                {role.name
                                  .charAt(0)
                                  .toUpperCase()}
                              </div>

                              <div>
                                <strong>
                                  {role.name}
                                </strong>

                                <span>
                                  {role.id}
                                </span>
                              </div>
                            </div>
                          </td>

                          <td>
                            <span
                              className={
                                role.systemRole
                                  ? 'role-type-badge role-type-system'
                                  : 'role-type-badge'
                              }
                            >
                              {role.systemRole
                                ? 'Системная'
                                : 'Пользовательская'}
                            </span>
                          </td>

                          <td>
                            <span
                              className={
                                role.active
                                  ? 'user-status user-status-active'
                                  : 'user-status user-status-inactive'
                              }
                            >
                              {role.active
                                ? 'Активна'
                                : 'Неактивна'}
                            </span>
                          </td>

                          <td>
                            <div className="role-description">
                              {role.description ??
                                '—'}
                            </div>
                          </td>

                          <td>
                            <div className="user-actions role-actions">
                              <button
                                className="table-action"
                                type="button"
                                disabled={
                                  role.systemRole ||
                                  isProcessing
                                }
                                onClick={() =>
                                  openEditModal(
                                    role,
                                  )
                                }
                              >
                                Редактировать
                              </button>

                              {role.active ? (
                                <button
                                  className="table-action table-action-danger"
                                  type="button"
                                  disabled={
                                    role.systemRole ||
                                    isProcessing
                                  }
                                  onClick={() => {
                                    void handleDeactivate(
                                      role,
                                    )
                                  }}
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
                                      role,
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
            aria-labelledby="create-role-title"
          >
            <div className="modal-header">
              <div>
                <p className="dashboard-eyebrow">
                  RBAC
                </p>

                <h2 id="create-role-title">
                  Новая роль
                </h2>

                <p>
                  Создание новой роли
                  разграничения доступа.
                </p>
              </div>

              <button
                className="modal-close"
                type="button"
                onClick={
                  closeCreateModal
                }
                disabled={isCreating}
                aria-label="Закрыть"
              >
                ×
              </button>
            </div>

            <form
              className="create-user-form"
              onSubmit={
                handleCreateRole
              }
            >
              <div className="form-grid">
                <label>
                  Код роли

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
                    placeholder="RISK_MANAGER"
                    maxLength={100}
                    pattern="[A-Z][A-Z0-9_]*"
                    required
                  />
                </label>

                <label>
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
                    placeholder="Менеджер рисков"
                    maxLength={150}
                    required
                  />
                </label>

                <label className="form-field-wide">
                  Описание

                  <textarea
                    className="role-textarea"
                    value={
                      createForm.description
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'description',
                        event.target.value,
                      )
                    }
                    placeholder="Описание назначения и области ответственности роли..."
                    maxLength={1000}
                    rows={5}
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
                  disabled={isCreating}
                >
                  Отмена
                </button>

                <button
                  className="primary-action"
                  type="submit"
                  disabled={isCreating}
                >
                  {isCreating
                    ? 'Создание...'
                    : 'Создать роль'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

      {editingRole &&
        editForm && (
          <div className="modal-backdrop">
            <section
              className="create-user-modal role-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="edit-role-title"
            >
              <div className="modal-header">
                <div>
                  <p className="dashboard-eyebrow">
                    RBAC
                  </p>

                  <h2 id="edit-role-title">
                    Редактирование роли
                  </h2>

                  <p>
                    Код роли после
                    создания не изменяется.
                  </p>
                </div>

                <button
                  className="modal-close"
                  type="button"
                  onClick={
                    closeEditModal
                  }
                  disabled={isUpdating}
                  aria-label="Закрыть"
                >
                  ×
                </button>
              </div>

              <div className="modal-info">
                <strong>
                  {editingRole.code}
                </strong>

                <span>
                  Код роли
                </span>
              </div>

              <form
                className="create-user-form"
                onSubmit={
                  handleUpdateRole
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
                      className="role-textarea"
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
                      rows={5}
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
                    disabled={isUpdating}
                  >
                    Отмена
                  </button>

                  <button
                    className="primary-action"
                    type="submit"
                    disabled={isUpdating}
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

function sortRoles(
  roles: RoleResponse[],
) {
  return [...roles].sort(
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
      const fieldErrors =
        Object.values(
          body.fieldErrors,
        )

      if (fieldErrors.length > 0) {
        return fieldErrors.join('. ')
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
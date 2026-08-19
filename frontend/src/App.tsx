import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
  ReactNode,
} from 'react'

import './App.css'

import DepartmentsPage from './DepartmentsPage'
import RolesPage from './RolesPage'

type UserStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'LOCKED'

type AuthTokenResponse = {
  tokenType: string
  accessToken: string
  accessTokenExpiresAt: string
  refreshToken: string
  refreshTokenExpiresAt: string
}

type UserResponse = {
  id: string
  employeeNumber: string
  username: string
  email: string
  firstName: string
  lastName: string
  departmentId: string | null
  departmentCode: string | null
  departmentName: string | null
  status: UserStatus
  failedLoginAttempts: number
  lockedUntil: string | null
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

type DepartmentResponse = {
  id: string
  code: string
  name: string
  parentId: string | null
  active: boolean
  createdAt: string
  updatedAt: string
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

type CreateUserForm = {
  employeeNumber: string
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
  departmentId: string
}

type EditUserForm = {
  employeeNumber: string
  username: string
  email: string
  firstName: string
  lastName: string
  departmentId: string
  status: UserStatus
}

type AdminLayoutProps = {
  active:
    | 'dashboard'
    | 'users'
    | 'departments'
    | 'roles'
    | 'permissions'
    | 'audit'
  eyebrow: string
  title: string
  description: string
  children: ReactNode
}

const emptyCreateUserForm: CreateUserForm = {
  employeeNumber: '',
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  departmentId: '',
}

function App() {
  const path = window.location.pathname

  if (path === '/dashboard') {
    return <DashboardPage />
  }

  if (path === '/users') {
    return <UsersPage />
  }

if (path === '/departments') {
  return (
    <DepartmentsPage
      layout={(children) => (
        <AdminLayout
          active="departments"
          eyebrow="ORGANIZATION"
          title="Подразделения"
          description="Управление организационной структурой и иерархией подразделений."
        >
          {children}
        </AdminLayout>
      )}
    />
  )
}

if (path === '/roles') {
  return (
    <RolesPage
      layout={(children) => (
        <AdminLayout
          active="roles"
          eyebrow="RBAC"
          title="Роли"
          description="Управление ролями и моделями разграничения доступа."
        >
          {children}
        </AdminLayout>
      )}
    />
  )
}

  return <LoginPage />
}

function AdminLayout({
  active,
  eyebrow,
  title,
  description,
  children,
}: AdminLayoutProps) {
  return (
    <main className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-mark">
            B
          </div>

          <div>
            <p className="brand-name">
              Bank Access
            </p>

            <p className="brand-subtitle">
              Management System
            </p>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button
            className={
              active === 'dashboard'
                ? 'nav-item active'
                : 'nav-item'
            }
            onClick={() => {
              window.location.href =
                '/dashboard'
            }}
          >
            Обзор
          </button>

          <button
            className={
              active === 'users'
                ? 'nav-item active'
                : 'nav-item'
            }
            onClick={() => {
              window.location.href =
                '/users'
            }}
          >
            Пользователи
          </button>

          <button
            className={
              active === 'departments'
                ? 'nav-item active'
                : 'nav-item'
            }
            onClick={() => {
              window.location.href =
                '/departments'
            }}
          >
            Подразделения
          </button>

          <button
            className={
              active === 'roles'
                ? 'nav-item active'
                : 'nav-item'
            }
            onClick={() => {
              window.location.href =
                '/roles'
            }}
          >
            Роли
          </button>

          <button
            className={
              active === 'permissions'
                ? 'nav-item active'
                : 'nav-item'
            }
          >
            Разрешения
          </button>

          <button
            className={
              active === 'audit'
                ? 'nav-item active'
                : 'nav-item'
            }
          >
            Журнал аудита
          </button>
        </nav>

        <div className="sidebar-footer">
          <button
            className="logout-button"
            onClick={() => {
              sessionStorage.clear()
              window.location.href = '/'
            }}
          >
            Выйти
          </button>
        </div>
      </aside>

      <section className="dashboard-content">
        <header className="dashboard-header">
          <div>
            <p className="dashboard-eyebrow">
              {eyebrow}
            </p>

            <h1>{title}</h1>

            <p>{description}</p>
          </div>

          <div className="current-user">
            <div className="user-avatar">
              A
            </div>

            <div>
              <strong>admin</strong>
              <span>Administrator</span>
            </div>
          </div>
        </header>

        {children}
      </section>
    </main>
  )
}

function DashboardPage() {
  return (
    <AdminLayout
      active="dashboard"
      eyebrow="OVERVIEW"
      title="Панель управления"
      description="Управление доступом и контроль административных операций."
    >
      <section className="dashboard-stats">
        <article className="stat-card">
          <p>Пользователи</p>
          <strong>—</strong>
          <span>
            Зарегистрировано в системе
          </span>
        </article>

        <article className="stat-card">
          <p>Роли</p>
          <strong>—</strong>
          <span>
            Модели разграничения доступа
          </span>
        </article>

        <article className="stat-card">
          <p>Разрешения</p>
          <strong>—</strong>
          <span>
            Доступные системные операции
          </span>
        </article>

        <article className="stat-card">
          <p>События аудита</p>
          <strong>—</strong>
          <span>
            Последние действия пользователей
          </span>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <div className="card-heading">
            <div>
              <p className="dashboard-eyebrow">
                ACCESS MANAGEMENT
              </p>

              <h2>
                Управление доступом
              </h2>
            </div>
          </div>

          <p>
            Создавайте пользователей,
            назначайте роли и контролируйте
            разрешения в рамках RBAC-модели.
          </p>

          <div className="quick-actions">
            <button
              onClick={() => {
                window.location.href =
                  '/users'
              }}
            >
              Пользователи
            </button>

            <button>
              Роли
            </button>

            <button>
              Разрешения
            </button>
          </div>
        </article>

        <article className="dashboard-card">
          <div className="card-heading">
            <div>
              <p className="dashboard-eyebrow">
                SECURITY
              </p>

              <h2>
                Журнал аудита
              </h2>
            </div>

            <span className="status-badge">
              ACTIVE
            </span>
          </div>

          <p>
            Действия пользователей
            регистрируются для последующего
            контроля и анализа.
          </p>

          <div className="audit-placeholder">
            Данные аудита подключим
            на следующем этапе
          </div>
        </article>
      </section>
    </AdminLayout>
  )
}

function UsersPage() {
  const [users, setUsers] =
    useState<UserResponse[]>([])

  const [departments, setDepartments] =
    useState<DepartmentResponse[]>([])

  const [search, setSearch] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [
    isDepartmentsLoading,
    setIsDepartmentsLoading,
  ] = useState(false)

  const [error, setError] =
    useState<string | null>(null)

  const [
    departmentsError,
    setDepartmentsError,
  ] = useState<string | null>(null)

  const [isCreateOpen, setIsCreateOpen] =
    useState(false)

  const [isCreating, setIsCreating] =
    useState(false)

  const [
    createError,
    setCreateError,
  ] = useState<string | null>(null)

  const [createForm, setCreateForm] =
    useState<CreateUserForm>(
      emptyCreateUserForm,
    )

  const [editUser, setEditUser] =
    useState<UserResponse | null>(null)

  const [editForm, setEditForm] =
    useState<EditUserForm | null>(null)

  const [isUpdating, setIsUpdating] =
    useState(false)

  const [editError, setEditError] =
    useState<string | null>(null)

  const [
    deactivatingUserId,
    setDeactivatingUserId,
  ] = useState<string | null>(null)

  useEffect(() => {
    void loadUsers()
    void loadDepartments()
  }, [])

  async function loadUsers() {
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
        '/api/users',
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
          'Недостаточно прав для просмотра пользователей',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось загрузить пользователей: HTTP ${response.status}`,
          ),
        )
      }

      const data: UserResponse[] =
        await response.json()

      setUsers(data)
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить пользователей',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  async function loadDepartments() {
    const accessToken =
      getAccessToken()

    if (!accessToken) {
      redirectToLogin()
      return
    }

    setIsDepartmentsLoading(true)
    setDepartmentsError(null)

    try {
      const response = await fetch(
        '/api/departments',
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
          'Недостаточно прав для просмотра подразделений',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось загрузить подразделения: HTTP ${response.status}`,
          ),
        )
      }

      const data: DepartmentResponse[] =
        await response.json()

      setDepartments(data)
    } catch (exception) {
      setDepartmentsError(
        getErrorMessage(
          exception,
          'Не удалось загрузить подразделения',
        ),
      )
    } finally {
      setIsDepartmentsLoading(false)
    }
  }

  function openCreateModal() {
    setCreateError(null)

    setCreateForm(
      emptyCreateUserForm,
    )

    setIsCreateOpen(true)

    if (
      departments.length === 0 &&
      !isDepartmentsLoading
    ) {
      void loadDepartments()
    }
  }

  function closeCreateModal() {
    if (isCreating) {
      return
    }

    setIsCreateOpen(false)
    setCreateError(null)

    setCreateForm(
      emptyCreateUserForm,
    )
  }

  function updateCreateField(
    field: keyof CreateUserForm,
    value: string,
  ) {
    setCreateForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  async function handleCreateUser(
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
        '/api/users',
        {
          method: 'POST',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify(
            createForm,
          ),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для создания пользователя',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось создать пользователя: HTTP ${response.status}`,
          ),
        )
      }

      const createdUser: UserResponse =
        await response.json()

      setUsers((current) => [
        createdUser,
        ...current,
      ])

      setIsCreateOpen(false)

      setCreateForm(
        emptyCreateUserForm,
      )
    } catch (exception) {
      setCreateError(
        getErrorMessage(
          exception,
          'Не удалось создать пользователя',
        ),
      )
    } finally {
      setIsCreating(false)
    }
  }

  function openEditModal(
    user: UserResponse,
  ) {
    setEditError(null)
    setEditUser(user)

    setEditForm({
      employeeNumber:
        user.employeeNumber,

      username:
        user.username,

      email:
        user.email,

      firstName:
        user.firstName,

      lastName:
        user.lastName,

      departmentId:
        user.departmentId ?? '',

      status:
        user.status,
    })

    if (
      departments.length === 0 &&
      !isDepartmentsLoading
    ) {
      void loadDepartments()
    }
  }

  function closeEditModal() {
    if (isUpdating) {
      return
    }

    setEditUser(null)
    setEditForm(null)
    setEditError(null)
  }

  function updateEditField(
    field: keyof EditUserForm,
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

  async function handleUpdateUser(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (!editUser || !editForm) {
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
        `/api/users/${editUser.id}`,
        {
          method: 'PUT',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify(
            editForm,
          ),
        },
      )

      if (response.status === 401) {
        redirectToLogin()
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для редактирования пользователя',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось обновить пользователя: HTTP ${response.status}`,
          ),
        )
      }

      const updatedUser: UserResponse =
        await response.json()

      setUsers((current) =>
        current.map((user) =>
          user.id === updatedUser.id
            ? updatedUser
            : user,
        ),
      )

      setEditUser(null)
      setEditForm(null)
    } catch (exception) {
      setEditError(
        getErrorMessage(
          exception,
          'Не удалось обновить пользователя',
        ),
      )
    } finally {
      setIsUpdating(false)
    }
  }

  async function handleDeactivateUser(
    user: UserResponse,
  ) {
    if (
      user.username.toLowerCase() ===
      'admin'
    ) {
      setError(
        'Деактивация основной учётной записи admin через интерфейс запрещена.',
      )

      return
    }

    if (user.status === 'INACTIVE') {
      return
    }

    const confirmed =
      window.confirm(
        `Деактивировать пользователя ${user.firstName} ${user.lastName} (${user.username})?\n\nАктивные сессии пользователя будут отозваны.`,
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

    setError(null)

    setDeactivatingUserId(
      user.id,
    )

    try {
      const response = await fetch(
        `/api/users/${user.id}`,
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
          'Недостаточно прав для деактивации пользователя',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось деактивировать пользователя: HTTP ${response.status}`,
          ),
        )
      }

      setUsers((current) =>
        current.map((currentUser) =>
          currentUser.id === user.id
            ? {
                ...currentUser,
                status: 'INACTIVE',
                lockedUntil: null,
              }
            : currentUser,
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось деактивировать пользователя',
        ),
      )
    } finally {
      setDeactivatingUserId(null)
    }
  }

  const filteredUsers = useMemo(() => {
    const query =
      search.trim().toLowerCase()

    if (!query) {
      return users
    }

    return users.filter((user) => {
      const searchableText = [
        user.employeeNumber,
        user.username,
        user.email,
        user.firstName,
        user.lastName,
        user.departmentCode ?? '',
        user.departmentName ?? '',
        user.status,
      ]
        .join(' ')
        .toLowerCase()

      return searchableText.includes(query)
    })
  }, [search, users])

  return (
    <AdminLayout
      active="users"
      eyebrow="ACCESS MANAGEMENT"
      title="Пользователи"
      description="Управление корпоративными учётными записями и состоянием доступа."
    >
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
            placeholder="Поиск по имени, логину, email или подразделению..."
          />
        </div>

        <button
          className="primary-action"
          type="button"
          onClick={openCreateModal}
        >
          + Добавить пользователя
        </button>
      </section>

      <section className="users-panel">
        <div className="users-panel-header">
          <div>
            <h2>
              Список пользователей
            </h2>

            <p>
              Найдено: {filteredUsers.length}
            </p>
          </div>

          <button
            className="refresh-button"
            type="button"
            onClick={() => {
              void loadUsers()
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
            Загрузка пользователей...
          </div>
        )}

        {!isLoading &&
          !error &&
          filteredUsers.length === 0 && (
            <div className="users-state">
              Пользователи не найдены
            </div>
          )}

        {!isLoading &&
          filteredUsers.length > 0 && (
            <div className="users-table-wrapper">
              <table className="users-table">
                <thead>
                  <tr>
                    <th>Пользователь</th>
                    <th>Логин</th>
                    <th>Подразделение</th>
                    <th>Статус</th>
                    <th>Последний вход</th>
                    <th>Действия</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredUsers.map(
                    (user) => {
                      const isAdmin =
                        user.username
                          .toLowerCase() ===
                        'admin'

                      const isDeactivating =
                        deactivatingUserId ===
                        user.id

                      return (
                        <tr key={user.id}>
                          <td>
                            <div className="table-user">
                              <div className="table-avatar">
                                {(
                                  user.firstName?.[0] ??
                                  user.username[0] ??
                                  'U'
                                ).toUpperCase()}
                              </div>

                              <div>
                                <strong>
                                  {user.firstName}{' '}
                                  {user.lastName}
                                </strong>

                                <span>
                                  {user.email}
                                </span>
                              </div>
                            </div>
                          </td>

                          <td>
                            <strong>
                              {user.username}
                            </strong>

                            <div className="employee-number">
                              {user.employeeNumber}
                            </div>
                          </td>

                          <td>
                            {user.departmentName ??
                              '—'}
                          </td>

                          <td>
                            <UserStatusBadge
                              status={user.status}
                            />
                          </td>

                          <td>
                            {formatDateTime(
                              user.lastLoginAt,
                            )}
                          </td>

                          <td>
                            <div className="user-actions">
                              <button
                                className="table-action"
                                type="button"
                                onClick={() =>
                                  openEditModal(
                                    user,
                                  )
                                }
                              >
                                Редактировать
                              </button>

                              <button
                                className="table-action table-action-danger"
                                type="button"
                                disabled={
                                  isAdmin ||
                                  user.status ===
                                    'INACTIVE' ||
                                  isDeactivating
                                }
                                onClick={() => {
                                  void handleDeactivateUser(
                                    user,
                                  )
                                }}
                                title={
                                  isAdmin
                                    ? 'Основную учётную запись admin нельзя деактивировать'
                                    : undefined
                                }
                              >
                                {isDeactivating
                                  ? 'Деактивация...'
                                  : 'Деактивировать'}
                              </button>
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
            className="create-user-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-user-title"
          >
            <div className="modal-header">
              <div>
                <p className="dashboard-eyebrow">
                  NEW ACCOUNT
                </p>

                <h2 id="create-user-title">
                  Новый пользователь
                </h2>

                <p>
                  Создание корпоративной
                  учётной записи.
                </p>
              </div>

              <button
                className="modal-close"
                type="button"
                onClick={closeCreateModal}
                disabled={isCreating}
                aria-label="Закрыть"
              >
                ×
              </button>
            </div>

            <form
              className="create-user-form"
              onSubmit={handleCreateUser}
            >
              <div className="form-grid">
                <label>
                  Имя

                  <input
                    type="text"
                    value={
                      createForm.firstName
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'firstName',
                        event.target.value,
                      )
                    }
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Фамилия

                  <input
                    type="text"
                    value={
                      createForm.lastName
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'lastName',
                        event.target.value,
                      )
                    }
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Табельный номер

                  <input
                    type="text"
                    value={
                      createForm.employeeNumber
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'employeeNumber',
                        event.target.value,
                      )
                    }
                    placeholder="EMP-1005"
                    maxLength={50}
                    required
                  />
                </label>

                <label>
                  Логин

                  <input
                    type="text"
                    value={
                      createForm.username
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'username',
                        event.target.value,
                      )
                    }
                    placeholder="a.smirnov"
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Email

                  <input
                    type="email"
                    value={
                      createForm.email
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'email',
                        event.target.value,
                      )
                    }
                    placeholder="user@bank.local"
                    maxLength={255}
                    required
                  />
                </label>

                <label>
                  Пароль

                  <input
                    type="password"
                    value={
                      createForm.password
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'password',
                        event.target.value,
                      )
                    }
                    minLength={8}
                    maxLength={100}
                    autoComplete="new-password"
                    required
                  />
                </label>

                <label className="form-field-wide">
                  Подразделение

                  <select
                    value={
                      createForm.departmentId
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'departmentId',
                        event.target.value,
                      )
                    }
                    disabled={
                      isDepartmentsLoading
                    }
                    required
                  >
                    <option value="">
                      {isDepartmentsLoading
                        ? 'Загрузка подразделений...'
                        : 'Выберите подразделение'}
                    </option>

                    {departments.map(
                      (department) => (
                        <option
                          key={department.id}
                          value={department.id}
                        >
                          {department.code}
                          {' — '}
                          {department.name}
                        </option>
                      ),
                    )}
                  </select>
                </label>
              </div>

              {(createError ||
                departmentsError) && (
                <div
                  className="login-error"
                  role="alert"
                >
                  {createError ??
                    departmentsError}
                </div>
              )}

              <div className="modal-actions">
                <button
                  className="secondary-action"
                  type="button"
                  onClick={closeCreateModal}
                  disabled={isCreating}
                >
                  Отмена
                </button>

                <button
                  className="primary-action"
                  type="submit"
                  disabled={
                    isCreating ||
                    isDepartmentsLoading
                  }
                >
                  {isCreating
                    ? 'Создание...'
                    : 'Создать пользователя'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

      {editUser && editForm && (
        <div className="modal-backdrop">
          <section
            className="create-user-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="edit-user-title"
          >
            <div className="modal-header">
              <div>
                <p className="dashboard-eyebrow">
                  USER MANAGEMENT
                </p>

                <h2 id="edit-user-title">
                  Редактирование пользователя
                </h2>

                <p>
                  Изменение данных и состояния
                  корпоративной учётной записи.
                </p>
              </div>

              <button
                className="modal-close"
                type="button"
                onClick={closeEditModal}
                disabled={isUpdating}
                aria-label="Закрыть"
              >
                ×
              </button>
            </div>

            <form
              className="create-user-form"
              onSubmit={handleUpdateUser}
            >
              <div className="form-grid">
                <label>
                  Имя

                  <input
                    type="text"
                    value={
                      editForm.firstName
                    }
                    onChange={(event) =>
                      updateEditField(
                        'firstName',
                        event.target.value,
                      )
                    }
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Фамилия

                  <input
                    type="text"
                    value={
                      editForm.lastName
                    }
                    onChange={(event) =>
                      updateEditField(
                        'lastName',
                        event.target.value,
                      )
                    }
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Табельный номер

                  <input
                    type="text"
                    value={
                      editForm.employeeNumber
                    }
                    onChange={(event) =>
                      updateEditField(
                        'employeeNumber',
                        event.target.value,
                      )
                    }
                    maxLength={50}
                    required
                  />
                </label>

                <label>
                  Логин

                  <input
                    type="text"
                    value={
                      editForm.username
                    }
                    onChange={(event) =>
                      updateEditField(
                        'username',
                        event.target.value,
                      )
                    }
                    maxLength={100}
                    required
                  />
                </label>

                <label>
                  Email

                  <input
                    type="email"
                    value={
                      editForm.email
                    }
                    onChange={(event) =>
                      updateEditField(
                        'email',
                        event.target.value,
                      )
                    }
                    maxLength={255}
                    required
                  />
                </label>

                <label>
                  Статус

                  <select
                    value={
                      editForm.status
                    }
                    onChange={(event) =>
                      updateEditField(
                        'status',
                        event.target.value,
                      )
                    }
                    disabled={
                      isUpdating ||
                      editUser.username
                        .toLowerCase() ===
                        'admin'
                    }
                    required
                  >
                    <option value="ACTIVE">
                      Активен
                    </option>

                    <option value="INACTIVE">
                      Неактивен
                    </option>

                    <option value="LOCKED">
                      Заблокирован
                    </option>
                  </select>
                </label>

                <label className="form-field-wide">
                  Подразделение

                  <select
                    value={
                      editForm.departmentId
                    }
                    onChange={(event) =>
                      updateEditField(
                        'departmentId',
                        event.target.value,
                      )
                    }
                    disabled={
                      isDepartmentsLoading
                    }
                    required
                  >
                    <option value="">
                      Выберите подразделение
                    </option>

                    {departments.map(
                      (department) => (
                        <option
                          key={department.id}
                          value={department.id}
                        >
                          {department.code}
                          {' — '}
                          {department.name}
                        </option>
                      ),
                    )}
                  </select>
                </label>
              </div>

              {editUser.username
                .toLowerCase() ===
                'admin' && (
                <div className="modal-info">
                  Статус основной учётной
                  записи admin нельзя изменить
                  через интерфейс.
                </div>
              )}

              {(editError ||
                departmentsError) && (
                <div
                  className="login-error"
                  role="alert"
                >
                  {editError ??
                    departmentsError}
                </div>
              )}

              <div className="modal-actions">
                <button
                  className="secondary-action"
                  type="button"
                  onClick={closeEditModal}
                  disabled={isUpdating}
                >
                  Отмена
                </button>

                <button
                  className="primary-action"
                  type="submit"
                  disabled={
                    isUpdating ||
                    isDepartmentsLoading
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
    </AdminLayout>
  )
}

function UserStatusBadge({
  status,
}: {
  status: UserStatus
}) {
  const labels: Record<
    UserStatus,
    string
  > = {
    ACTIVE: 'Активен',
    INACTIVE: 'Неактивен',
    LOCKED: 'Заблокирован',
  }

  return (
    <span
      className={
        `user-status user-status-${status.toLowerCase()}`
      }
    >
      {labels[status]}
    </span>
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

function formatDateTime(
  value: string | null,
) {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat(
    'ru-RU',
    {
      dateStyle: 'short',
      timeStyle: 'short',
    },
  ).format(new Date(value))
}

function LoginPage() {
  const [username, setUsername] =
    useState('')

  const [password, setPassword] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(false)

  const [error, setError] =
    useState<string | null>(null)

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    setError(null)
    setIsLoading(true)

    try {
      const response = await fetch(
        '/api/auth/login',
        {
          method: 'POST',

          headers: {
            'Content-Type':
              'application/json',
          },

          body: JSON.stringify({
            username,
            password,
          }),
        },
      )

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error(
            'Неверный логин или пароль',
          )
        }

        throw new Error(
          `Ошибка авторизации: HTTP ${response.status}`,
        )
      }

      const auth: AuthTokenResponse =
        await response.json()

      sessionStorage.setItem(
        'accessToken',
        auth.accessToken,
      )

      sessionStorage.setItem(
        'refreshToken',
        auth.refreshToken,
      )

      sessionStorage.setItem(
        'accessTokenExpiresAt',
        auth.accessTokenExpiresAt,
      )

      window.location.href =
        '/dashboard'
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось выполнить вход',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel">
        <div className="brand">
          <div className="brand-mark">
            B
          </div>

          <div>
            <p className="brand-name">
              Bank Access
            </p>

            <p className="brand-subtitle">
              Management System
            </p>
          </div>
        </div>

        <div className="login-heading">
          <p className="eyebrow">
            SECURE ACCESS
          </p>

          <h1>Вход в систему</h1>

          <p>
            Используйте корпоративную
            учётную запись для доступа
            к панели управления.
          </p>
        </div>

        <form
          className="login-form"
          onSubmit={handleSubmit}
        >
          <label>
            Имя пользователя

            <input
              type="text"
              value={username}
              onChange={(event) =>
                setUsername(
                  event.target.value,
                )
              }
              autoComplete="username"
              placeholder="admin"
              maxLength={100}
              required
            />
          </label>

          <label>
            Пароль

            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(
                  event.target.value,
                )
              }
              autoComplete="current-password"
              placeholder="Введите пароль"
              maxLength={100}
              required
            />
          </label>

          {error && (
            <div
              className="login-error"
              role="alert"
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
          >
            {isLoading
              ? 'Выполняется вход...'
              : 'Войти'}
          </button>
        </form>

        <p className="security-note">
          Доступ к системе контролируется
          политиками RBAC и регистрируется
          в журнале аудита.
        </p>
      </section>

      <section className="login-visual">
        <div className="visual-content">
          <p className="eyebrow">
            BANK SECURITY PLATFORM
          </p>

          <h2>
            Управление доступом.
            <br />
            Контроль действий.
            <br />
            Защита данных.
          </h2>

          <p>
            Централизованное управление
            пользователями, ролями,
            разрешениями и аудитом
            административных операций.
          </p>
        </div>
      </section>
    </main>
  )
}

export default App
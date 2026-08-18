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
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  failedLoginAttempts: number
  lockedUntil: string | null
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
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

function App() {
  const path = window.location.pathname

  if (path === '/dashboard') {
    return <DashboardPage />
  }

  if (path === '/users') {
    return <UsersPage />
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
          >
            Подразделения
          </button>

          <button
            className={
              active === 'roles'
                ? 'nav-item active'
                : 'nav-item'
            }
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

  const [search, setSearch] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    void loadUsers()
  }, [])

  async function loadUsers() {
    const accessToken =
      sessionStorage.getItem(
        'accessToken',
      )

    if (!accessToken) {
      window.location.href = '/'
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
        sessionStorage.clear()
        window.location.href = '/'
        return
      }

      if (response.status === 403) {
        throw new Error(
          'Недостаточно прав для просмотра пользователей',
        )
      }

      if (!response.ok) {
        throw new Error(
          `Не удалось загрузить пользователей: HTTP ${response.status}`,
        )
      }

      const data: UserResponse[] =
        await response.json()

      setUsers(data)
    } catch (exception) {
      if (exception instanceof Error) {
        setError(exception.message)
      } else {
        setError(
          'Не удалось загрузить пользователей',
        )
      }
    } finally {
      setIsLoading(false)
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

        {isLoading && (
          <div className="users-state">
            Загрузка пользователей...
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

        {!isLoading &&
          !error &&
          filteredUsers.length === 0 && (
            <div className="users-state">
              Пользователи не найдены
            </div>
          )}

        {!isLoading &&
          !error &&
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
                  </tr>
                </thead>

                <tbody>
                  {filteredUsers.map(
                    (user) => (
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
                      </tr>
                    ),
                  )}
                </tbody>
              </table>
            </div>
          )}
      </section>
    </AdminLayout>
  )
}

function UserStatusBadge({
  status,
}: {
  status: UserResponse['status']
}) {
  const labels = {
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
      if (exception instanceof Error) {
        setError(exception.message)
      } else {
        setError(
          'Не удалось выполнить вход',
        )
      }
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
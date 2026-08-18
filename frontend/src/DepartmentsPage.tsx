import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  FormEvent,
  ReactNode,
} from 'react'

export type DepartmentResponse = {
  id: string
  code: string
  name: string
  parentId: string | null
  active: boolean
  createdAt: string
  updatedAt: string
}

type DepartmentForm = {
  code: string
  name: string
  parentId: string
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

type DepartmentsPageProps = {
  layout: (children: ReactNode) => ReactNode
}

const emptyDepartmentForm: DepartmentForm = {
  code: '',
  name: '',
  parentId: '',
}

export default function DepartmentsPage({
  layout,
}: DepartmentsPageProps) {
  const [
    departments,
    setDepartments,
  ] = useState<DepartmentResponse[]>([])

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

  const [isCreating, setIsCreating] =
    useState(false)

  const [
    createError,
    setCreateError,
  ] = useState<string | null>(null)

  const [createForm, setCreateForm] =
    useState<DepartmentForm>(
      emptyDepartmentForm,
    )

  const [
    editingDepartment,
    setEditingDepartment,
  ] =
    useState<DepartmentResponse | null>(
      null,
    )

  const [editForm, setEditForm] =
    useState<DepartmentForm | null>(
      null,
    )

  const [isUpdating, setIsUpdating] =
    useState(false)

  const [editError, setEditError] =
    useState<string | null>(null)

  const [
    deactivatingDepartmentId,
    setDeactivatingDepartmentId,
  ] = useState<string | null>(null)

  useEffect(() => {
    void loadDepartments()
  }, [])

  async function loadDepartments() {
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

      setDepartments(
        sortDepartments(data),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить подразделения',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  function openCreateModal() {
    setCreateError(null)

    setCreateForm(
      emptyDepartmentForm,
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
      emptyDepartmentForm,
    )
  }

  function updateCreateField(
    field: keyof DepartmentForm,
    value: string,
  ) {
    setCreateForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  async function handleCreate(
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
        '/api/departments',
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

            parentId:
              createForm.parentId ||
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
          'Недостаточно прав для создания подразделения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось создать подразделение: HTTP ${response.status}`,
          ),
        )
      }

      const created:
        DepartmentResponse =
          await response.json()

      setDepartments((current) =>
        sortDepartments([
          ...current,
          created,
        ]),
      )

      setIsCreateOpen(false)

      setCreateForm(
        emptyDepartmentForm,
      )
    } catch (exception) {
      setCreateError(
        getErrorMessage(
          exception,
          'Не удалось создать подразделение',
        ),
      )
    } finally {
      setIsCreating(false)
    }
  }

  function openEditModal(
    department:
      DepartmentResponse,
  ) {
    setEditError(null)

    setEditingDepartment(
      department,
    )

    setEditForm({
      code: department.code,
      name: department.name,

      parentId:
        department.parentId ?? '',
    })
  }

  function closeEditModal() {
    if (isUpdating) {
      return
    }

    setEditingDepartment(null)
    setEditForm(null)
    setEditError(null)
  }

  function updateEditField(
    field: keyof DepartmentForm,
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

  async function handleUpdate(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    if (
      !editingDepartment ||
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
        `/api/departments/${editingDepartment.id}`,
        {
          method: 'PUT',

          headers: {
            'Content-Type':
              'application/json',

            Authorization:
              `Bearer ${accessToken}`,
          },

          body: JSON.stringify({
            code: editForm.code,
            name: editForm.name,

            parentId:
              editForm.parentId ||
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
          'Недостаточно прав для редактирования подразделения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось обновить подразделение: HTTP ${response.status}`,
          ),
        )
      }

      const updated:
        DepartmentResponse =
          await response.json()

      setDepartments((current) =>
        sortDepartments(
          current.map(
            (department) =>
              department.id ===
              updated.id
                ? updated
                : department,
          ),
        ),
      )

      setEditingDepartment(null)
      setEditForm(null)
    } catch (exception) {
      setEditError(
        getErrorMessage(
          exception,
          'Не удалось обновить подразделение',
        ),
      )
    } finally {
      setIsUpdating(false)
    }
  }

  async function handleDeactivate(
    department:
      DepartmentResponse,
  ) {
    const confirmed =
      window.confirm(
        `Деактивировать подразделение «${department.name}» (${department.code})?\n\nОно перестанет отображаться среди активных подразделений.`,
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

    setDeactivatingDepartmentId(
      department.id,
    )

    try {
      const response = await fetch(
        `/api/departments/${department.id}`,
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
          'Недостаточно прав для деактивации подразделения',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось деактивировать подразделение: HTTP ${response.status}`,
          ),
        )
      }

      setDepartments((current) =>
        current.filter(
          (item) =>
            item.id !==
            department.id,
        ),
      )
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось деактивировать подразделение',
        ),
      )
    } finally {
      setDeactivatingDepartmentId(
        null,
      )
    }
  }

  const departmentMap =
    useMemo(() => {
      return new Map(
        departments.map(
          (department) => [
            department.id,
            department,
          ],
        ),
      )
    }, [departments])

  const filteredDepartments =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase()

      if (!query) {
        return departments
      }

      return departments.filter(
        (department) => {
          const parent =
            department.parentId
              ? departmentMap.get(
                  department.parentId,
                )
              : null

          const searchableText = [
            department.code,
            department.name,
            parent?.code ?? '',
            parent?.name ?? '',
          ]
            .join(' ')
            .toLowerCase()

          return searchableText.includes(
            query,
          )
        },
      )
    }, [
      departments,
      departmentMap,
      search,
    ])

  const parentOptionsForEdit =
    useMemo(() => {
      if (!editingDepartment) {
        return departments
      }

      const forbiddenIds =
        collectDescendantIds(
          editingDepartment.id,
          departments,
        )

      forbiddenIds.add(
        editingDepartment.id,
      )

      return departments.filter(
        (department) =>
          !forbiddenIds.has(
            department.id,
          ),
      )
    }, [
      departments,
      editingDepartment,
    ])

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
            placeholder="Поиск по коду, названию или родительскому подразделению..."
          />
        </div>

        <button
          className="primary-action"
          type="button"
          onClick={
            openCreateModal
          }
        >
          + Добавить подразделение
        </button>
      </section>

      <section className="users-panel departments-panel">
        <div className="users-panel-header">
          <div>
            <h2>
              Список подразделений
            </h2>

            <p>
              Активных: {
                filteredDepartments.length
              }
            </p>
          </div>

          <button
            className="refresh-button"
            type="button"
            onClick={() => {
              void loadDepartments()
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
            Загрузка подразделений...
          </div>
        )}

        {!isLoading &&
          filteredDepartments.length ===
            0 && (
            <div className="users-state">
              Подразделения не найдены
            </div>
          )}

        {!isLoading &&
          filteredDepartments.length >
            0 && (
            <div className="users-table-wrapper">
              <table className="users-table departments-table">
                <thead>
                  <tr>
                    <th>Код</th>
                    <th>
                      Подразделение
                    </th>
                    <th>
                      Родительское
                    </th>
                    <th>Статус</th>
                    <th>Создано</th>
                    <th>Действия</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredDepartments.map(
                    (department) => {
                      const parent =
                        department.parentId
                          ? departmentMap.get(
                              department.parentId,
                            )
                          : null

                      const isDeactivating =
                        deactivatingDepartmentId ===
                        department.id

                      return (
                        <tr
                          key={
                            department.id
                          }
                        >
                          <td>
                            <span className="department-code">
                              {
                                department.code
                              }
                            </span>
                          </td>

                          <td>
                            <div className="department-name-cell">
                              <div className="department-icon">
                                {
                                  department
                                    .name[0]
                                }
                              </div>

                              <div>
                                <strong>
                                  {
                                    department.name
                                  }
                                </strong>

                                <span>
                                  {
                                    department.id
                                  }
                                </span>
                              </div>
                            </div>
                          </td>

                          <td>
                            {parent
                              ? `${parent.code} — ${parent.name}`
                              : department.parentId
                                ? 'Недоступно'
                                : 'Корневое подразделение'}
                          </td>

                          <td>
                            <span className="user-status user-status-active">
                              Активно
                            </span>
                          </td>

                          <td>
                            {formatDateTime(
                              department.createdAt,
                            )}
                          </td>

                          <td>
                            <div className="user-actions">
                              <button
                                className="table-action"
                                type="button"
                                onClick={() =>
                                  openEditModal(
                                    department,
                                  )
                                }
                              >
                                Редактировать
                              </button>

                              <button
                                className="table-action table-action-danger"
                                type="button"
                                disabled={
                                  isDeactivating
                                }
                                onClick={() => {
                                  void handleDeactivate(
                                    department,
                                  )
                                }}
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
            className="create-user-modal department-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-department-title"
          >
            <div className="modal-header">
              <div>
                <p className="dashboard-eyebrow">
                  ORGANIZATION
                </p>

                <h2 id="create-department-title">
                  Новое подразделение
                </h2>

                <p>
                  Добавление подразделения
                  в организационную структуру.
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
                handleCreate
              }
            >
              <div className="form-grid">
                <label>
                  Код подразделения

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
                    placeholder="RISK"
                    maxLength={50}
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
                    placeholder="Управление рисками"
                    maxLength={150}
                    required
                  />
                </label>

                <label className="form-field-wide">
                  Родительское подразделение

                  <select
                    value={
                      createForm.parentId
                    }
                    onChange={(event) =>
                      updateCreateField(
                        'parentId',
                        event.target.value,
                      )
                    }
                  >
                    <option value="">
                      Нет — корневое подразделение
                    </option>

                    {departments.map(
                      (department) => (
                        <option
                          key={
                            department.id
                          }
                          value={
                            department.id
                          }
                        >
                          {
                            department.code
                          }
                          {' — '}
                          {
                            department.name
                          }
                        </option>
                      ),
                    )}
                  </select>
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
                    : 'Создать подразделение'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

      {editingDepartment &&
        editForm && (
          <div className="modal-backdrop">
            <section
              className="create-user-modal department-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="edit-department-title"
            >
              <div className="modal-header">
                <div>
                  <p className="dashboard-eyebrow">
                    ORGANIZATION
                  </p>

                  <h2 id="edit-department-title">
                    Редактирование подразделения
                  </h2>

                  <p>
                    Изменение названия,
                    кода и положения
                    в организационной
                    структуре.
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

              <form
                className="create-user-form"
                onSubmit={
                  handleUpdate
                }
              >
                <div className="form-grid">
                  <label>
                    Код подразделения

                    <input
                      type="text"
                      value={
                        editForm.code
                      }
                      onChange={(event) =>
                        updateEditField(
                          'code',
                          event.target.value,
                        )
                      }
                      maxLength={50}
                      required
                    />
                  </label>

                  <label>
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
                    Родительское подразделение

                    <select
                      value={
                        editForm.parentId
                      }
                      onChange={(event) =>
                        updateEditField(
                          'parentId',
                          event.target.value,
                        )
                      }
                    >
                      <option value="">
                        Нет — корневое подразделение
                      </option>

                      {parentOptionsForEdit.map(
                        (department) => (
                          <option
                            key={
                              department.id
                            }
                            value={
                              department.id
                            }
                          >
                            {
                              department.code
                            }
                            {' — '}
                            {
                              department.name
                            }
                          </option>
                        ),
                      )}
                    </select>
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

function sortDepartments(
  departments: DepartmentResponse[],
) {
  return [...departments].sort(
    (left, right) =>
      left.name.localeCompare(
        right.name,
        'ru',
      ),
  )
}

function collectDescendantIds(
  departmentId: string,
  departments: DepartmentResponse[],
) {
  const result = new Set<string>()

  let changed = true

  while (changed) {
    changed = false

    for (const department of departments) {
      if (
        result.has(department.id)
      ) {
        continue
      }

      if (
        department.parentId ===
          departmentId ||
        (
          department.parentId !== null &&
          result.has(
            department.parentId,
          )
        )
      ) {
        result.add(
          department.id,
        )

        changed = true
      }
    }
  }

  return result
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
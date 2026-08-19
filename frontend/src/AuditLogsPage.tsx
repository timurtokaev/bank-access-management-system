import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import type {
  ReactNode,
} from 'react'

type AuditLogResponse = {
  id: string
  actorUserId: string | null
  actorUsername: string | null
  action: string
  entityType: string | null
  entityId: string | null
  result: string
  ipAddress: string | null
  details: unknown
  occurredAt: string
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

type AuditLogsPageProps = {
  layout: (children: ReactNode) => ReactNode
}

export default function AuditLogsPage({
  layout,
}: AuditLogsPageProps) {
  const [logs, setLogs] =
    useState<AuditLogResponse[]>([])

  const [search, setSearch] =
    useState('')

  const [isLoading, setIsLoading] =
    useState(true)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    void loadAuditLogs()
  }, [])

  async function loadAuditLogs() {
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
        '/api/audit-logs',
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
          'Недостаточно прав для просмотра журнала аудита',
        )
      }

      if (!response.ok) {
        throw new Error(
          await readApiError(
            response,
            `Не удалось загрузить журнал аудита: HTTP ${response.status}`,
          ),
        )
      }

      const data: AuditLogResponse[] =
        await response.json()

      setLogs(data)
    } catch (exception) {
      setError(
        getErrorMessage(
          exception,
          'Не удалось загрузить журнал аудита',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  }

  const filteredLogs =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase()

      if (!query) {
        return logs
      }

      return logs.filter((log) => {
        const searchable = [
          log.actorUsername ?? '',
          log.actorUserId ?? '',
          log.action,
          log.entityType ?? '',
          log.entityId ?? '',
          log.result,
          log.ipAddress ?? '',
          formatDetails(log.details),
          log.occurredAt,
        ]
          .join(' ')
          .toLowerCase()

        return searchable.includes(
          query,
        )
      })
    }, [logs, search])

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
            placeholder="Поиск по пользователю, действию, сущности, результату или IP..."
          />
        </div>

        <button
          className="refresh-button"
          type="button"
          onClick={() => {
            void loadAuditLogs()
          }}
          disabled={isLoading}
        >
          Обновить
        </button>
      </section>

      <section className="users-panel audit-panel">
        <div className="users-panel-header">
          <div>
            <h2>
              Последние события
            </h2>

            <p>
              Показано:{' '}
              {filteredLogs.length}
              {' из '}
              {logs.length}
            </p>
          </div>

          <div className="audit-live-indicator">
            <span />
            Последние 100 записей
          </div>
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
            Загрузка журнала аудита...
          </div>
        )}

        {!isLoading &&
          !error &&
          filteredLogs.length === 0 && (
            <div className="users-state">
              События не найдены
            </div>
          )}

        {!isLoading &&
          filteredLogs.length > 0 && (
            <div className="users-table-wrapper">
              <table className="users-table audit-table">
                <thead>
                  <tr>
                    <th>Время</th>
                    <th>Пользователь</th>
                    <th>Действие</th>
                    <th>Сущность</th>
                    <th>Результат</th>
                    <th>IP</th>
                    <th>Детали</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredLogs.map(
                    (log) => (
                      <tr key={log.id}>
                        <td>
                          <div className="audit-time">
                            <strong>
                              {formatTime(
                                log.occurredAt,
                              )}
                            </strong>

                            <span>
                              {formatDate(
                                log.occurredAt,
                              )}
                            </span>
                          </div>
                        </td>

                        <td>
                          <div className="audit-actor">
                            <div className="table-avatar audit-avatar">
                              {getActorInitial(
                                log.actorUsername,
                              )}
                            </div>

                            <div>
                              <strong>
                                {log.actorUsername ??
                                  'system'}
                              </strong>

                              <span>
                                {log.actorUserId ??
                                  '—'}
                              </span>
                            </div>
                          </div>
                        </td>

                        <td>
                          <span className="audit-action">
                            {formatEnum(
                              log.action,
                            )}
                          </span>
                        </td>

                        <td>
                          <div className="audit-entity">
                            <strong>
                              {log.entityType
                                ? formatEnum(
                                    log.entityType,
                                  )
                                : '—'}
                            </strong>

                            <span>
                              {log.entityId ??
                                '—'}
                            </span>
                          </div>
                        </td>

                        <td>
                          <span
                            className={
                              getResultClassName(
                                log.result,
                              )
                            }
                          >
                            {formatEnum(
                              log.result,
                            )}
                          </span>
                        </td>

                        <td>
                          <span className="audit-ip">
                            {log.ipAddress ??
                              '—'}
                          </span>
                        </td>

                        <td>
                          <AuditDetails
                            details={
                              log.details
                            }
                          />
                        </td>
                      </tr>
                    ),
                  )}
                </tbody>
              </table>
            </div>
          )}
      </section>
    </>,
  )
}

function AuditDetails({
  details,
}: {
  details: unknown
}) {
  const formatted =
    formatDetails(details)

  if (
    !formatted ||
    formatted === '{}' ||
    formatted === 'null'
  ) {
    return (
      <span className="audit-empty-details">
        —
      </span>
    )
  }

  return (
    <details className="audit-details">
      <summary>
        Показать
      </summary>

      <pre>
        {formatted}
      </pre>
    </details>
  )
}

function getResultClassName(
  result: string,
) {
  const normalized =
    result.toLowerCase()

  if (
    normalized.includes('success')
  ) {
    return 'audit-result audit-result-success'
  }

  if (
    normalized.includes('fail') ||
    normalized.includes('denied') ||
    normalized.includes('error')
  ) {
    return 'audit-result audit-result-failure'
  }

  return 'audit-result'
}

function getActorInitial(
  username: string | null,
) {
  if (!username) {
    return 'S'
  }

  return username
    .charAt(0)
    .toUpperCase()
}

function formatEnum(
  value: string,
) {
  return value
    .replaceAll('_', ' ')
    .trim()
}

function formatDate(
  value: string,
) {
  return new Intl.DateTimeFormat(
    'ru-RU',
    {
      dateStyle: 'short',
    },
  ).format(new Date(value))
}

function formatTime(
  value: string,
) {
  return new Intl.DateTimeFormat(
    'ru-RU',
    {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    },
  ).format(new Date(value))
}

function formatDetails(
  details: unknown,
) {
  if (details === null ||
      details === undefined) {
    return ''
  }

  if (
    typeof details === 'string'
  ) {
    return details
  }

  try {
    return JSON.stringify(
      details,
      null,
      2,
    )
  } catch {
    return String(details)
  }
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
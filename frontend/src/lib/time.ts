const rtf = new Intl.RelativeTimeFormat('tr', { numeric: 'auto' })

const UNITS: Array<[Intl.RelativeTimeFormatUnit, number]> = [
  ['year', 365 * 24 * 3600],
  ['month', 30 * 24 * 3600],
  ['week', 7 * 24 * 3600],
  ['day', 24 * 3600],
  ['hour', 3600],
  ['minute', 60],
]

export function timeAgo(iso: string): string {
  const seconds = Math.round((new Date(iso).getTime() - Date.now()) / 1000)
  const abs = Math.abs(seconds)
  for (const [unit, unitSeconds] of UNITS) {
    if (abs >= unitSeconds) {
      return rtf.format(Math.trunc(seconds / unitSeconds), unit)
    }
  }
  return 'şimdi'
}

function getCookie(name: string): string | undefined {
  const cookies = document.cookie.split(";")
  for (const cookie of cookies) {
    const [key, value] = cookie.trim().split("=")
    if (key === name) {
      return decodeURIComponent(value)
    }
  }
  return undefined
}

export default getCookie

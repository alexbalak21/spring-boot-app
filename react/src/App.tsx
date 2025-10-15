import axios from "axios"
import {useState} from "react"

const URL = "/api/message"

export default function App() {
  const [message, setMessage] = useState("")
  function handleClick() {
    axios.get(URL).then((response) => {
      setMessage(response.data.message)
    })
  }

  return (
    <main>
      <h1>React App</h1>
      <p>{message}</p>
      <button onClick={handleClick}>Fetch Data</button>
    </main>
  )
}

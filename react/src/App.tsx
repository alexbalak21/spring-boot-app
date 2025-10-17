import axios from "axios"
import {useState} from "react"
import getCookie from "./getCookie"

const URL = "/api"

export default function App() {
  const [responseMessage, setResponseMessage] = useState("")
  const [inputValue, setInputValue] = useState("")

  function handleGet() {
    console.log("handleGet triggered")
    axios
      .get(URL)
      .then((response) => {
        console.log("GET response:", response)
        setResponseMessage(response.data.message)
      })
      .catch((error) => {
        console.error("GET request failed:", error)
        if (error.response) {
          console.error("Response data:", error.response.data)
          console.error("Response status:", error.response.status)
          console.error("Response headers:", error.response.headers)
        } else if (error.request) {
          console.error("No response received:", error.request)
        } else {
          console.error("Error message:", error.message)
        }
      })
  }

  function handlePost() {
    const xsrfToken = getCookie("XSRF-TOKEN")

    axios
      .post(
        URL,
        {message: inputValue},
        {
          headers: {
            "X-XSRF-TOKEN": xsrfToken ?? "",
          },
          withCredentials: true,
        }
      )
      .then((response) => {
        setResponseMessage(response.data.message)
      })
      .catch((error) => {
        console.error("POST request failed:", error)
        if (error.response) {
          console.error("Response data:", error.response.data)
          console.error("Response status:", error.response.status)
        } else if (error.request) {
          console.error("No response received:", error.request)
        } else {
          console.error("Error message:", error.message)
        }
        setResponseMessage("Error: Could not send message.")
      })
  }
  return (
    <main>
      <h1>React App</h1>
      <h2>Get</h2>
      <button onClick={handleGet}>Get</button>
      <h2>Post</h2>
      <input type="text" value={inputValue} onChange={(e) => setInputValue(e.target.value)} />
      <br />
      <button onClick={handlePost}>Post</button>
      <h2>Response Message</h2>
      <p>{responseMessage}</p>
    </main>
  )
}

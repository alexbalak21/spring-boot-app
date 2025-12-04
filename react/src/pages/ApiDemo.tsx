import { useState } from "react";
import axios from "axios";
import styles from "./ApiDemo.module.css";
import { useCsrf } from "../hooks/useCsrf"; // adjust path to your hook

// Axios defaults for XSRF and cookies
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = "XSRF-TOKEN";
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

const API_BASE = "/api";

export default function ApiDemo() {
  const [input, setInput] = useState("");
  const [responseText, setResponseText] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // ✅ use the custom hook
  const csrfReady = useCsrf();

  const handlePost = async () => {
    if (!input.trim()) {
      setResponseText("Enter a message before sending");
      return;
    }

    setLoading(true);
    setResponseText(null);
    try {
      const res = await axios.post(API_BASE, { message: input });
      setResponseText(JSON.stringify(res.data, null, 2));
      setInput("");
    } catch (err: any) {
      const serverMessage =
        err?.response?.data?.message || err.message || "Unknown error";
      setResponseText(`Error: ${serverMessage}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>API Demo</h1>
      <div className={styles.container}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type a message..."
          className={styles.input}
        />
        <button
          onClick={handlePost}
          disabled={loading || !csrfReady} // ✅ block until CSRF ready
          className={`${styles.button} ${loading ? styles.buttonDisabled : ""}`}
        >
          {loading ? "Sending..." : "Send Message"}
        </button>
      </div>
      {responseText && (
        <div className={styles.responseBox}>
          {responseText}
        </div>
      )}
    </div>
  );
}

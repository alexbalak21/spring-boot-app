import { useEffect, useState } from "react";
import axios from "axios";

// Axios defaults for XSRF and cookies
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = "XSRF-TOKEN";
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

const API_BASE = "/api";

export default function App() {
  const [input, setInput] = useState("");
  const [responseText, setResponseText] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Fetch CSRF token once on mount (backend should set XSRF-TOKEN cookie)
  useEffect(() => {
    axios
      .get(`${API_BASE}/csrf`)
      .then(() => {
        // cookie should be set by server; axios will use it automatically
      })
      .catch((err) => {
        console.error("Failed to fetch CSRF token", err);
      });
  }, []);

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
      console.error("POST error", err);
      const serverMessage = err?.response?.data?.message || err.message || "Unknown error";
      setResponseText(`Error: ${serverMessage}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 20, fontFamily: "Arial, sans-serif", maxWidth: 800 }}>
      <h1>Minimal React + Spring CSRF demo</h1>

      <div style={{ marginBottom: 12 }}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type a message"
          style={{ padding: 8, width: "70%", marginRight: 8 }}
        />
        <button onClick={handlePost} disabled={loading} style={{ padding: "8px 12px" }}>
          {loading ? "Sending..." : "Send"}
        </button>
      </div>

      <div style={{ marginBottom: 12 }}>
        <strong>Response:</strong>
        <pre style={{ background: "#f6f6f6", padding: 12, whiteSpace: "pre-wrap" }}>
          {responseText ?? "No response yet"}
        </pre>
      </div>

      <div>
        <strong>Current cookies:</strong>
        <pre style={{ background: "#f6f6f6", padding: 12 }}>{document.cookie || "No cookies"}</pre>
      </div>
    </div>
  );
}

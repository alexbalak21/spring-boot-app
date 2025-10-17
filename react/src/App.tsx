import axios from "axios";
import { useState, useEffect } from "react";

const URL = "/api";

// Configure axios defaults
axios.defaults.withCredentials = true;
axios.defaults.withXSRFToken = true;

// Add a request interceptor
axios.interceptors.request.use(
  (config) => {
    const token = getCookie("XSRF-TOKEN");
    if (token && !["get", "head", "options"].includes(config.method?.toLowerCase() || "")) {
      config.headers["X-XSRF-TOKEN"] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Helper function to get cookie by name
function getCookie(name: string): string | null {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
  return null;
}

export default function App() {
  const [responseMessage, setResponseMessage] = useState("");
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Fetch CSRF token when component mounts
  useEffect(() => {
    const fetchCsrfToken = async () => {
      try {
        await axios.get('/api/csrf', { withCredentials: true });
        console.log('CSRF token fetched successfully');
      } catch (error) {
        console.error('Error fetching CSRF token:', error);
      }
    };
    fetchCsrfToken();
  }, []);

  const handlePost = async () => {
    if (!inputValue.trim()) {
      setResponseMessage("Please enter a message");
      return;
    }

    setIsLoading(true);
    try {
      const response = await axios.post(URL, { message: inputValue });
      setResponseMessage(JSON.stringify(response.data, null, 2));
      setInputValue("");
    } catch (error: any) {
      console.error("POST error:", {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        headers: error.response?.headers,
        cookies: document.cookie
      });
      setResponseMessage(`Error: ${error.response?.data?.message || error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>React + Spring Boot with CSRF</h1>
      <div>
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Type a message"
        />
        <button onClick={handlePost} disabled={isLoading}>
          {isLoading ? 'Sending...' : 'Send'}
        </button>
      </div>
      <div>
        <h3>Response:</h3>
        <pre style={{ whiteSpace: 'pre-wrap' }}>
          {responseMessage || 'No response yet'}
        </pre>
      </div>
      <div>
        <h4>Current Cookies:</h4>
        <pre>{document.cookie || 'No cookies found'}</pre>
      </div>
    </div>
  );
}
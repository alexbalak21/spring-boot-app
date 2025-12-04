import { useEffect, useState } from "react";
import axios from "axios";

// Axios defaults for CSRF
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = "XSRF-TOKEN";
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

export function useCsrf() {
  const [csrfReady, setCsrfReady] = useState(false);

  useEffect(() => {
    const fetchCsrfToken = async () => {
      try {
        const res = await axios.get("/api/csrf");
        console.log("✅ CSRF token loaded:", res.status, res.data);
        setCsrfReady(true);
      } catch (err) {
        console.error("Failed to fetch CSRF token", err);
      }
    };
    fetchCsrfToken();
  }, []);

  return csrfReady;
}

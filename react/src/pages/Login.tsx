import { useState } from "react";
import axios from "axios";
import styles from "./Login.module.css";
import { useCsrf } from "../hooks/useCsrf"; // adjust path

const LOGIN_URL = "/api/auth/login";

interface LoginFormData {
  username: string;
  password: string;
}

export default function Login() {
  const csrfReady = useCsrf();

  const [formData, setFormData] = useState<LoginFormData>({
    username: "",
    password: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [loginResult, setLoginResult] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoginResult(null);

    if (!formData.username.trim() || !formData.password) {
      setError("Please enter both username and password");
      return;
    }

    setIsLoading(true);

    try {
      const response = await axios.post(LOGIN_URL, formData, {
        headers: {
          "Content-Type": "application/json",
          "X-Requested-With": "XMLHttpRequest",
        },
      });

      setLoginResult(response.data?.message || "Login successful");
      console.log("Login successful:", response.data);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Login failed. Please try again.";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h2 className={styles.title}>Login</h2>

        {error && <div className={styles.error}>{error}</div>}
        {loginResult && <div className={styles.success}>{loginResult}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formGroup}>
            <label htmlFor="username" className={styles.label}>
              Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className={styles.input}
              disabled={isLoading}
              autoComplete="username"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.label}>
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={styles.input}
              disabled={isLoading}
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className={`${styles.button} ${isLoading ? styles.buttonLoading : ""}`}
            disabled={isLoading || !csrfReady}
          >
            {isLoading ? "Logging in..." : "Login"}
          </button>
        </form>
      </div>
    </div>
  );
}

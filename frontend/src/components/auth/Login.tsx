import { useState } from "react"
import { Container, Row, Col, Form, Button } from "react-bootstrap"
import { useNavigate } from "react-router"
import { loginCall } from "../../services/authService"
import { PiUserLight } from "react-icons/pi"
import { CiWarning } from "react-icons/ci"
import "../../styles/login.css"
import { useDispatch } from "react-redux"
import { setUser } from "../../redux/reducers/userSlice"

const Login = () => {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  const storedPhoto = localStorage.getItem("photoUrl")
  const userPhoto =
    storedPhoto && storedPhoto !== "undefined" ? storedPhoto : null

  const dispatch = useDispatch()

  const handleSubmit = async (e: React.SubmitEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")

    try {
      const response = await loginCall({ email, password })

      console.log("LOGIN RESPONSE:", response)

      // Salviamo i token
      localStorage.setItem("accessToken", response.accessToken)
      localStorage.setItem("refreshToken", response.refreshToken)
      localStorage.setItem("photoUrl", response.photoUrl)

      const roleName = response.roleName

      // array di ruoli
      dispatch(
        setUser({
          photoUrl: response.photoUrl,
          roleNames: [roleName],
        }),
      )

      // Redirect in base al ruolo
      if (roleName === "ADMIN") {
        navigate("/dashboard/admin")
      } else if (roleName === "HR") {
        navigate("/dashboard/hr")
      } else if (roleName === "SHIFT MANAGER") {
        navigate("/dashboard/shift-manager")
      } else {
        navigate("/dashboard/staff")
      }
    } catch (err: unknown) {
      setError((err as Error).message || "Credenziali errate")
      setIsLoading(false)
    }
  }
  return (
    <Container fluid className="min-vh-100 p-0">
      <Row className="g-0 min-vh-100 justify-content-center">
        {/* COLONNA SINISTRA: Brand, Mood e Identità visiva */}
        <Col
          xs={12}
          md={8}
          lg={6}
          className="background-brand d-none d-lg-flex flex-column justify-content-between p-5 text-white position-relative overflow-hidden"
        >
          <div className="position-absolute top-0 start-0 w-100 h-100 opacity-10 bg-grid"></div>

          {/* Top Logo */}
          <div className="z-1">
            <h3 className="fw-bold tracking-wide">YouRoster</h3>
          </div>

          {/* Centro: Frase d'impatto */}
          <div className="z-1 my-auto py-5">
            <h1 className="display-4 fw-bold mb-3">
              La gestione turni, resa semplice.
            </h1>
            <p className="lead opacity-75">
              Coordina il personale, ottimizza le risorse e gestisci ogni team
              con un'interfaccia pensata per chi lavora.
            </p>
          </div>

          {/* Footer del pannello sinistro */}
          <div className="z-1">
            <small className="opacity-75">
              YouRoster - {new Date().getFullYear()}
            </small>
          </div>
        </Col>

        {/* COLONNA DESTRA: Login */}
        <Col
          xs={12}
          lg={6}
          className="d-flex align-items-center justify-content-center background2 p-4 p-md-5 min-vh-100 position-relative overflow-hidden"
        >
          <div className="d-flex flex-column position-relative z-1 w-100">
            <h1 className="fs-1 text-center mb-4 d-lg-none brand-title">
              YouRoster
            </h1>
            <div className="w-100">
              <div className="mb-4 text-center text-lg-start">
                <h2 className="fw-bold mb-1 text-primary text-center small-title">
                  Bentornato
                </h2>
                <p className="text-muted text-center small-text">
                  Inserisci le tue credenziali per accedere al gestionale.
                </p>
              </div>

              <div className="box mt-4 p-4 rounded-4 shadow-lg">
                <div>
                  <div className="avatar-circle bg-light d-flex justify-content-center align-items-center">
                    {userPhoto ? (
                      <img
                        src={userPhoto}
                        alt="Avatar Profilo"
                        className="w-100 h-100"
                        style={{ objectFit: "cover" }}
                      />
                    ) : (
                      <div className="w-100 h-100 rounded-circle overflow-hidden bg-light border border-4 border-secondary d-flex align-items-center justify-content-center">
                        <PiUserLight size={100} className="text-secondary" />
                      </div>
                    )}
                  </div>
                </div>

                <div className="d-flex justify-content-center">
                  {error && (
                    <div className="small-text p-1 text-center text-warning my-4 py-2 border border-warning border-1 rounded-2 w-75">
                      <p className="mb-1">
                        <CiWarning size={30} />
                      </p>
                      <p className="text-center m-0">
                        {error.includes("email") ? (
                          <>
                            L'email <strong className="fw-bold">{email}</strong>{" "}
                            non esiste nel nostro sistema
                          </>
                        ) : (
                          error
                        )}
                      </p>
                    </div>
                  )}
                </div>

                <Form onSubmit={handleSubmit}>
                  <div className="d-flex flex-column align-items-center">
                    <Form.Group className="mb-3 w-75" controlId="formEmail">
                      <Form.Label className="text-muted small-text">
                        Email
                      </Form.Label>
                      <Form.Control
                        className="input"
                        type="email"
                        placeholder="mario.rossi@example.com"
                        value={email}
                        onChange={(e) => {
                          setEmail(e.target.value)
                          setError("")
                        }}
                        required
                        autoComplete="false"
                      />
                    </Form.Group>

                    <Form.Group className="mb-3 w-75" controlId="formPassword">
                      <Form.Label className="text-muted small-text">
                        Password
                      </Form.Label>
                      <Form.Control
                        className="input"
                        type="password"
                        placeholder="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        autoComplete="new-password"
                      />
                    </Form.Group>

                    <div className="d-flex justify-content-start w-75">
                      {error.includes("Credenziali non valide") && (
                        <a
                          href="/forgot-password"
                          className="text-warning text-decoration-underline small-text"
                        >
                          Password dimenticata?
                        </a>
                      )}
                    </div>
                  </div>

                  <div className="d-flex justify-content-center mt-3">
                    <Button
                      className="btn-custom1 fw-bold small-text"
                      type="submit"
                      disabled={isLoading}
                    >
                      Login
                    </Button>
                  </div>
                </Form>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  )
}

export default Login

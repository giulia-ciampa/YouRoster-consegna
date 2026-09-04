import { useState } from "react"
import { useNavigate, useSearchParams } from "react-router"
import { forgotPassword, resetPassword } from "../../services/authService"
import {
  Col,
  Container,
  Row,
  Form,
  Button,
  ListGroup,
  ListGroupItem,
} from "react-bootstrap"
import "../../styles/mobileText.css"

const ResetPassword = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")
  const navigate = useNavigate()

  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const [emailForRecovery, setEmailForRecovery] = useState("")

  const [errorsList, setErrorsList] = useState<string[]>([])

  // Funzione per il reset della password
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setMessage("")
    setIsLoading(true)
    setError("")
    setErrorsList([])

    if (!token) {
      setError("Token di reset mancante.")
      setIsLoading(false)
      return
    }

    if (newPassword !== confirmPassword) {
      setError("Le password non coincidono.")
      setIsLoading(false)
      return
    }

    try {
      const data = await resetPassword(token, newPassword)
      setMessage(data)
      setTimeout(() => navigate("/login"), 3000)
    } catch (err: unknown) {
      const errorMsg = (err as Error).message
      try {
        const parsed = JSON.parse(errorMsg)
        setError(parsed.message || errorMsg)
        // Se il backend restituisce un array di errori di validazione, li salviamo
        if (parsed.errorsList && Array.isArray(parsed.errorsList)) {
          setErrorsList(parsed.errorsList)
        }
      } catch {
        setError(errorMsg || "Errore durante la richiesta.")
      }
    } finally {
      setIsLoading(false)
    }
  }

  // Funzione per inviare la nuova email se il token è scaduto
  const handleResendEmail = async (e: React.SubmitEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")
    setMessage("")

    try {
      const data = await forgotPassword(emailForRecovery)
      setMessage(
        data ||
          "Email di recupero inviata con successo! Controlla la tua posta.",
      )
    } catch (err: unknown) {
      const errorMsg = (err as Error).message
      try {
        const parsed = JSON.parse(errorMsg)
        setError(parsed.message || errorMsg)
      } catch {
        setError(errorMsg || "Errore durante l'invio dell'email.")
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Container fluid className="min-vh-100 p-0">
      <Row className="g-0 min-vh-100 justify-content-center">
        {/* COLONNA SINISTRA: Brand, Mood e Identità visiva */}
        <Col
          xs={12}
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

        {/*COLONNA DI DESTRA */}
        <Col
          xs={12}
          md={8}
          lg={6}
          className="d-flex flex-column align-items-center justify-content-center background2 p-4 p-md-5 position-relative overflow-hidden"
        >
          {/* Sfumature decorative di sfondo */}
          <div className="bg-glow-container">
            <div className="glow-blob-1"></div>
            <div className="glow-blob-2"></div>
          </div>

          <div className="w-75 text-center border-lg-0 p-5 border border-3 border-primary rounded-4 m-3 m-lg-0 shadow-lg">
            <h1 className="fs-5 text-center mb-4 d-lg-none">
              <span className="text-brand-orange fw-bold fs-1">You</span>
              <span className="text-brand-magenta fw-bold fs-1">Roster</span>
            </h1>

            {error.includes("recupero") ? (
              <h2 className="text-primary  mb-3 text-center small-title">
                Inserisci la tua email
              </h2>
            ) : (
              <h2 className="text-primary mb-3 text-center small-title">
                Inserisci la tua nuova password
              </h2>
            )}

            {!token ? (
              <div className="text-warning text-center small-text rounded-2 border border-1 border-warning">
                Non sei abilitato ad accedere a questa pagina.
              </div>
            ) : error.includes("recupero") ? (
              // --- FORM PER RICHIEDERE UNA NUOVA EMAIL SE IL TOKEN È SCADUTO ---
              <Form onSubmit={handleResendEmail}>
                <div className="d-flex flex-column align-items-center">
                  <Form.Group
                    className="mb-3 w-75 text-start"
                    controlId="formRecoveryEmail"
                  >
                    <Form.Label className="text-dark small-text">
                      Email
                    </Form.Label>
                    <Form.Control
                      className="border border-1 border-secondary"
                      type="email"
                      placeholder="nome@esempio.com"
                      value={emailForRecovery}
                      onChange={(e) => setEmailForRecovery(e.target.value)}
                      required
                    />
                  </Form.Group>
                </div>
                <div className="d-flex justify-content-center mt-3">
                  <Button
                    variant="outline-primary"
                    type="submit"
                    disabled={isLoading}
                  >
                    {isLoading ? "Invio in corso..." : "Invia Nuova Email"}
                  </Button>
                </div>
              </Form>
            ) : (
              // --- FORM ORIGINALE PER IL RESET DELLA PASSWORD ---
              <Form onSubmit={handleSubmit}>
                <div className="d-flex flex-column align-items-center">
                  <Form.Group
                    className="mb-3 w-75 text-start"
                    controlId="formNewPassword"
                  >
                    <Form.Label className="text-muted small-text">
                      Nuova password
                    </Form.Label>
                    <Form.Control
                      className="border border-2 border-secondary"
                      type="password"
                      placeholder="Nuova password"
                      value={newPassword}
                      onChange={(e) => {
                        setNewPassword(e.target.value)
                        setError("")
                      }}
                      required
                    />
                  </Form.Group>

                  <Form.Group
                    className="mb-3 w-75 text-start"
                    controlId="formConfirmPassword"
                  >
                    <Form.Label className="text-muted small-text">
                      Conferma nuova password
                    </Form.Label>
                    <Form.Control
                      className="border border-2 border-secondary"
                      type="password"
                      placeholder="Conferma password"
                      value={confirmPassword}
                      onChange={(e) => {
                        setConfirmPassword(e.target.value)
                        setError("")
                      }}
                      required
                    />
                  </Form.Group>
                </div>

                <div className="d-flex justify-content-center mt-3">
                  <Button
                    className="border border-3 border-primary fw-bold small-text"
                    variant="outline-primary"
                    type="submit"
                    disabled={isLoading}
                  >
                    Salva Password
                  </Button>
                </div>
              </Form>
            )}

            <div className="text-center d-flex justify-content-center">
              {message && (
                <p className="text-secondary border border-1 border-secondary p-1 mt-4 rounded-2 p-2 small-text">
                  {message}
                </p>
              )}
            </div>

            <div className="text-center d-flex justify-content-center">
              {error && (
                <div className="text-warning border border-1 border-warning p-2 mt-4 rounded-2 w-75 small-text">
                  {error.toLowerCase().includes("validation") &&
                  errorsList.length > 0 ? (
                    // Se è un errore di validazione, mostriamo SOLO la lista degli errori specifici
                    <ListGroup
                      variant="flush"
                      className="mb-0 text-start small"
                    >
                      {errorsList.map((errItem, index) => (
                        <ListGroupItem
                          key={index}
                          className="bg-transparent text-warning border-0"
                        >
                          {errItem}
                        </ListGroupItem>
                      ))}
                    </ListGroup>
                  ) : (
                    // Altrimenti mostriamo il normale messaggio di errore generico
                    <p className="mb-0">{error}</p>
                  )}
                </div>
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  )
}

export default ResetPassword

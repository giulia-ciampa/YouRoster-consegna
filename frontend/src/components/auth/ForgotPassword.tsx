import { useState } from "react"
import { forgotPassword } from "../../services/authService"
import { Col, Container, Row, Form, Button } from "react-bootstrap"
import { CiWarning } from "react-icons/ci"
import "../../styles/mobileText.css"

const ForgotPassword = () => {
  const [email, setEmail] = useState("")
  const [error, setError] = useState("")
  const [successMsg, setSuccessMsg] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (e: React.SubmitEvent) => {
    e.preventDefault()
    setError("")
    setSuccessMsg("")
    setIsLoading(true)

    try {
      await forgotPassword(email)
      setSuccessMsg(
        "Email di recupero inviata con successo! Controlla la posta.",
      )
    } catch (err: unknown) {
      // Estraiamo il messaggio dall'errore
      let message = (err as Error).message

      // Personalizziamo il messaggio se è quello del token duplicato o del database
      if (
        message.includes("duplicazione") ||
        message.includes("already exists")
      ) {
        message =
          "Hai già richiesto un reset di recente. Controlla la tua casella di posta!"
      }

      setError(message)
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

        {/*COLONNA DI DESTRA - RECUPER PASSWORD */}
        <Col
          xs={12}
          md={8}
          lg={6}
          className="d-flex align-items-center justify-content-center background2 p-4 min-vh-100 position-relative overflow-hidden"
        >
          <div className="d-flex flex-column box shadow-lg p-4 w-100">
            <h1 className="fs-1 text-center mb-4 d-lg-none brand-title">
              YouRoster
            </h1>

            <div className="mb-4 text-center">
              <h2 className="fw-bold mb-1 text-primary text-center small-title">
                Recupera la tua password
              </h2>
              <p className="text-muted small-text">
                Inserisci l'email per ricevere il link di modifica password.
              </p>
            </div>

            <Form onSubmit={handleSubmit}>
              <div className="d-flex justify-content-center">
                <Form.Group
                  className=" text-start mb-3 w-75"
                  controlId="formEmail"
                >
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
                    autoComplete="off"
                  />
                </Form.Group>
              </div>

              <div className="d-flex justify-content-center mt-3">
                <Button
                  className="btn-custom1 fw-bold small-text"
                  type="submit"
                  disabled={isLoading}
                >
                  Invia
                </Button>
              </div>
            </Form>

            <div className="d-flex flex-column align-items-center mt-3 small-text">
              {successMsg && (
                <div className="small-text p-1 d-flex flex-column align-items-center justify-content-center text-secondary my-2 py-2 border border-secondary border-1 rounded-2 w-75">
                  <p className="text-center m-0">{successMsg}</p>
                </div>
              )}

              {error && (
                <div className="small-text p-1 d-flex flex-column align-items-center justify-content-center text-warning my-2 py-2 border border-warning border-1 rounded-2 w-75">
                  <p className="mb-1">
                    <CiWarning size={30} />
                  </p>
                  <p className="text-center m-0">
                    {error.includes("non esiste nel nostro sistema") ? (
                      <>
                        L'email <strong className="fw-bold">{email}</strong> non
                        esiste nel nostro sistema
                      </>
                    ) : (
                      error
                    )}
                  </p>
                </div>
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  )
}

export default ForgotPassword

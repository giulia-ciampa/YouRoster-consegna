import { Button, Col, Container, Row } from "react-bootstrap"
import "../../styles/mobileText.css"
import { useNavigate } from "react-router"

const RegistrationPending = () => {
  const navigate = useNavigate()
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

        {/* COLONNA DESTRA: REGISTRATION PENDING */}
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

          <div className="w-100 m-lg-5 text-center p-4 box rounded-4 shadow-sm">
            <h1 className="fs-1 brand-title text-center mb-4 d-lg-none">
              YouRoster
            </h1>
            <h2 className="fw-bold mb-3 text-primary small-title">
              Registrazione avvenuta con successo
            </h2>
            <p className="text-muted mb-4 small-text">
              Grazie per aver completato la registrazione. Riceverai un'email
              non appena l'amministratore accetterà la tua richiesta.
            </p>

            {/* Eventuale pulsante per tornare al login o alla home */}
            <Button
              className="btn-custom1 small-text"
              onClick={() => navigate("/")}
            >
              Torna alla Home
            </Button>
          </div>
        </Col>
      </Row>
    </Container>
  )
}

export default RegistrationPending

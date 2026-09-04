import { useState } from "react"
import { PiUserCircleThin } from "react-icons/pi"
import { NavLink, useNavigate } from "react-router"
import type { UpdateCredentialsPayload } from "../../types/auth"
import { updateCredentials } from "../../services/authService"
import {
  Container,
  Modal,
  Nav,
  Navbar,
  NavDropdown,
  Form,
  Button,
} from "react-bootstrap"
import { IoIosArrowDown } from "react-icons/io"
import { useDispatch, useSelector } from "react-redux"
import type { RootState } from "../../redux/store/store"
import { clearUser } from "../../redux/reducers/userSlice"

export const UserNavbar = () => {
  const [openModal, setOpenModal] = useState(false)

  const [email, setEmail] = useState("")
  const [oldPassword, setOldPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [confirmNewPassword, setConfirmNewPassword] = useState("")

  const userPhoto = useSelector((state: RootState) => state.user.photoUrl)

  const userAvatar =
    userPhoto && userPhoto !== "undefined" ? (
      <img
        src={userPhoto}
        alt="User Avatar"
        className="avatar-circle"
        style={{ width: "35px", height: "35px", objectFit: "cover" }}
      />
    ) : (
      <PiUserCircleThin size={32} className="text-white" />
    )

  const navigate = useNavigate()
  const dispatch = useDispatch()

  //funzione logout
  const handleLogout = () => {
    dispatch(clearUser())
    navigate("/")
  }

  const handleSaveChanges = async () => {
    const payload: UpdateCredentialsPayload = {}

    if (email) payload.email = email
    if (oldPassword) payload.oldPassword = oldPassword
    if (newPassword) payload.newPassword = newPassword
    if (confirmNewPassword) payload.confirmNewPassword = confirmNewPassword

    try {
      await updateCredentials(payload)
      alert("Credenziali aggiornate con successo!")
      setOpenModal(false)

      setEmail("")
      setOldPassword("")
      setNewPassword("")
      setConfirmNewPassword("")
    } catch (error: unknown) {
      console.error("Errore durante l'aggiornamento:", error)

      const errMessage =
        error instanceof Error
          ? error.message
          : "Errore nell'aggiornamento delle credenziali."
      alert(errMessage)
    }
  }

  return (
    <Navbar expand="sm" className="background-brand px-3">
      <Container
        fluid
        className="d-flex justify-content-between align-items-center"
      >
        {/* PARTE SINISTRA: Logo YouRoster (desktop) o Menu a tendina (mobile) */}
        <div className="d-flex align-items-center">
          <Navbar.Brand
            as={NavLink}
            to={"/"}
            className="fw-bold tracking-wide text-light d-none d-sm-inline me-3"
          >
            YouRoster
          </Navbar.Brand>

          {/* Dropdown mobile */}
          <NavDropdown
            title={
              <span className="text-light fw-bold">
                YouRoster <IoIosArrowDown />
              </span>
            }
            id="mobile-menu-dropdown"
            className="d-sm-none myDropDown"
          >
            <NavDropdown.Item
              as={NavLink}
              to={"/dashboard/staff"}
              className="text-dark"
            >
              Dashboard
            </NavDropdown.Item>

            <NavDropdown.Item
              as={NavLink}
              to={"/personal-data/staff"}
              className="text-dark"
            >
              Anagrafica
            </NavDropdown.Item>

            <NavDropdown.Item
              as={NavLink}
              to={"/shifts/user"}
              className="text-dark"
            >
              Turni
            </NavDropdown.Item>

            <NavDropdown.Item
              as={NavLink}
              to={"/requests/user"}
              className="text-dark"
            >
              Richieste
            </NavDropdown.Item>

            <NavDropdown.Item
              as={NavLink}
              to={"/offices/staff"}
              className="text-dark"
            >
              Uffici
            </NavDropdown.Item>
          </NavDropdown>

          {/* Link desktop */}
          <Nav className="d-none d-sm-flex flex-row gap-3">
            <Nav.Link
              as={NavLink}
              to={"/dashboard/staff"}
              className="text-light"
            >
              Dashboard
            </Nav.Link>

            <Nav.Link
              as={NavLink}
              to={"/personal-data/staff"}
              className="text-light"
            >
              Anagrafica
            </Nav.Link>

            <Nav.Link as={NavLink} to={"/shifts/user"} className="text-light">
              Turni
            </Nav.Link>

            <Nav.Link as={NavLink} to={"/requests/user"} className="text-light">
              Richieste
            </Nav.Link>

            <Nav.Link as={NavLink} to={"/offices/staff"} className="text-light">
              Uffici
            </Nav.Link>
          </Nav>
        </div>

        {/* PARTE DESTRA: Avatar dell'admin (Sempre visibile e bloccato a destra) */}
        <Nav>
          <NavDropdown
            title={userAvatar}
            id="admin-nav-dropdown"
            align="end"
            className="custom-avatar-dropdown"
          >
            <NavDropdown.Item onClick={() => setOpenModal(true)}>
              Modifica credenziali
            </NavDropdown.Item>
            <NavDropdown.Divider />
            <NavDropdown.Item className="text-danger" onClick={handleLogout}>
              Logout
            </NavDropdown.Item>
          </NavDropdown>
        </Nav>
        {/* Esempio di modale con React-Bootstrap */}
        <Modal show={openModal} onHide={() => setOpenModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Modifica Credenziali</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {/* Qui inserisci il tuo form per email e password */}

            <h5 className="small-title text-dark">Modifica email</h5>

            <Form>
              <Form.Group className="mb-3" controlId="formEmail">
                <Form.Label className="small-text text-muted mt-1 mb-0">
                  Nuova Email
                </Form.Label>
                <Form.Control
                  type="email"
                  placeholder="mario.rossi@example.it"
                  className="input"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="false"
                />
              </Form.Group>

              <h5 className="small-title text-dark mt-4">Modifica password</h5>

              <Form.Group className="mb-3" controlId="formOldPassword">
                <Form.Label className="small-text text-muted mt-1 mb-0">
                  Inserisci la tua vecchia password
                </Form.Label>
                <Form.Control
                  type="password"
                  placeholder="******"
                  className="input"
                  value={oldPassword}
                  onChange={(e) => setOldPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </Form.Group>
              <Form.Group className="mb-3" controlId="formNewPassword">
                <Form.Label className="small-text text-muted mt-1 mb-0">
                  Inserisci la tua nuova password
                </Form.Label>
                <Form.Control
                  type="password"
                  placeholder="******"
                  className="input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </Form.Group>
              <Form.Group className="mb-3" controlId="formConfirmPassword">
                <Form.Label className="small-text text-muted mt-1 mb-0">
                  Conferma la nuova password
                </Form.Label>
                <Form.Control
                  type="password"
                  placeholder="******"
                  className="input"
                  value={confirmNewPassword}
                  onChange={(e) => setConfirmNewPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </Form.Group>
            </Form>
          </Modal.Body>
          <Modal.Footer>
            <Button
              className="btn-custom1"
              onClick={() => {
                handleSaveChanges()
                setOpenModal(false)
              }}
            >
              Salva modifiche
            </Button>
            <Button className="btn-custom2" onClick={() => setOpenModal(false)}>
              Chiudi
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </Navbar>
  )
}

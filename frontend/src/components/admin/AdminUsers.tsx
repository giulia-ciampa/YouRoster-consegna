import {
  Col,
  Container,
  Row,
  Form,
  Table,
  Card,
  Spinner,
  Button,
  Modal,
} from "react-bootstrap"
import AdminNavbar from "./AdminNavbar"
import { useEffect, useState } from "react"
import { FcSearch } from "react-icons/fc"
import type { AccountStatus, UserProfileResponseDTO } from "../../types/users"
import { getAllUsers, updateUserRoles } from "../../services/adminService"
import "../../styles/mobileText.css"
import { FaPencilAlt } from "react-icons/fa"
import { IoMdAdd } from "react-icons/io"
import {
  fetchActiveOffices,
  updateUserOffice,
} from "../../services/officeService"
import type { OfficeResponseDTO } from "../../types/office"
import { fetchAllRoles } from "../../services/roleService"
import type { RoleResponseDTO } from "../../types/role"

export const AdminUsers = () => {
  const [searchName, setSearchName] = useState("")
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<UserProfileResponseDTO[]>([])
  const [selectedOfficeId, setSelectedOfficeId] = useState("")
  const [offices, setOffices] = useState<OfficeResponseDTO[]>([])
  const [selectedUser, setSelectedUser] =
    useState<UserProfileResponseDTO | null>(null)
  const [showOfficeModal, setShowOfficeModal] = useState(false)

  const [showRoleModal, setShowRoleModal] = useState(false)
  const [roles, setRoles] = useState<RoleResponseDTO[]>([])
  const [selectedRoles, setSelectedRoles] = useState<string[]>([])

  //GET TUTTI GLI UTENTI
  const fetchUsers = async () => {
    try {
      setLoading(true)

      const response = await getAllUsers(0, 15)

      setUsers(response.content)
    } catch (error) {
      console.error("Errore nel recupero degli utenti:", error)
      alert(
        error instanceof Error
          ? error.message
          : "Errore nel recupero degli utenti.",
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchUsers()
  }, [])

  //GET TUTTI GLI UFFICI ATTVI
  const fetchOffices = async () => {
    try {
      const response = await fetchActiveOffices()

      setOffices(response)
    } catch (error) {
      console.error("Errore nel recupero delle sedi:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore nel recupero delle sedi.",
      )
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchUsers()
    fetchOffices()
  }, [])

  //FUNZIONE APERTURA MODALE UFFICI ATTIVI
  const handleOpenOfficeModal = async (user: UserProfileResponseDTO) => {
    try {
      setSelectedUser(user)

      // Se l'utente ha già una sede, la preseleziono
      // Altrimenti il select parte vuoto
      setSelectedOfficeId(user.officeId ?? "")

      const response = await fetchActiveOffices()

      setOffices(response)

      setShowOfficeModal(true)
    } catch (error) {
      console.error("Errore nel recupero delle sedi:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore nel recupero delle sedi.",
      )
    }
  }

  //FUNZIONE APERTURA MODALE RUOLI
  const handleOpenRoleModal = async (user: UserProfileResponseDTO) => {
    try {
      setSelectedUser(user)

      // Preseleziona i ruoli che l'utente ha già
      setSelectedRoles(user.roleNames)

      // Recupera tutti i ruoli disponibili
      const response = await fetchAllRoles()

      setRoles(response)

      setShowRoleModal(true)
    } catch (error) {
      console.error("Errore nel recupero dei ruoli:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore nel recupero dei ruoli.",
      )
    }
  }

  //FUNZIONE ASSEGNAZIONE UFFICIO
  const handleSaveOffice = async () => {
    if (!selectedUser || !selectedOfficeId) {
      return
    }

    try {
      setLoading(true)

      await updateUserOffice(selectedUser.userId, selectedOfficeId)

      alert("Sede aggiornata con successo.")

      setShowOfficeModal(false)
      setSelectedOfficeId("")

      await fetchUsers()
    } catch (error) {
      console.error("Errore nell'aggiornamento della sede:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore nell'aggiornamento della sede.",
      )
    } finally {
      setLoading(false)
    }
  }

  //FUNZIONE ASSEGNA RUOLO
  const handleSaveRoles = async () => {
    if (!selectedUser?.accountId) {
      return
    }

    try {
      setLoading(true)

      await updateUserRoles(selectedUser.accountId, selectedRoles)

      setUsers((prevUsers) =>
        prevUsers.map((user) =>
          user.userId === selectedUser.userId
            ? { ...user, roleNames: selectedRoles }
            : user,
        ),
      )

      alert("Ruoli aggiornati con successo.")

      setShowRoleModal(false)
    } catch (error) {
      console.error("Errore durante l'aggiornamento dei ruoli:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante l'aggiornamento dei ruoli.",
      )
    } finally {
      setLoading(false)
    }
  }

  //funzione per tradurre lo stato
  const translateStatus = (status: AccountStatus) => {
    switch (status) {
      case "ACTIVE":
        return "ATTIVO"
      case "DISABLED":
        return "DISABILITATO"
      case "PENDING":
        return "IN ATTESA"
      case "REJECTED":
        return "RIFIUTATO"
      default:
        return "NESSUNO STATO"
    }
  }

  //funzione per colorare lo stato
  const colorStatus = (status: AccountStatus) => {
    switch (status) {
      case "ACTIVE":
        return "text-success"
      case "PENDING":
        return "text-warning"
      case "DISABLED":
        return "text-danger"
      case "REJECTED":
        return "text-danger"
      default:
        return "text-dark"
    }
  }

  //funzione per tradurre i ruoli
  const translateRole = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "Amministratore"

      case "HR":
        return "Risorse Umane"

      case "SHIFT MANAGER":
        return "Responsabile Turni"

      case "STAFF":
        return "Staff"

      case "AP E PAYROLL SPECIALIST":
        return "Specialista Paghe"

      case "MANAGER":
        return "Manager"

      case "COORDINATOR":
        return "Coordinatore"

      default:
        return role
    }
  }

  const filteredUsers = users.filter((user) => {
    const fullName = `${user.name} ${user.surname}`.toLowerCase()

    return fullName.includes(searchName.toLowerCase())
  })

  return (
    <>
      <AdminNavbar />
      <Container
        fluid
        className="d-flex flex-column my-4 align-items-center flex-grow-1"
      >
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="ps-0">
            <h3 className="small-title text-dark mb-0">
              Informazioni sugli utenti
            </h3>
          </Col>
        </Row>
        {/*SEARCH*/}
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="px-0">
            <div className="d-flex justify-content-lg-start my-2">
              <Form>
                <Form.Group className="mb-3" controlId="search">
                  <Form.Label className="text-muted small-text mb-0">
                    Cerca dipendente
                  </Form.Label>
                  <div className="position-relative">
                    <FcSearch className="position-absolute top-50 start-0 translate-middle-y ms-2 text-muted" />

                    <Form.Control
                      type="text"
                      placeholder="Nome o cognome"
                      className="input ps-4 small-text"
                      value={searchName}
                      onChange={(e) => setSearchName(e.target.value)}
                    />
                  </div>
                </Form.Group>
              </Form>
            </div>

            {/*TABELLA */}
            {loading ? (
              <div className="d-flex justify-content-center py-4">
                <Spinner animation="border" variant="primary" />
              </div>
            ) : (
              <>
                <Card className="border border-1 border-secondary shadow-lg">
                  <div className="table-responsive m-2">
                    <Table
                      striped
                      bordered
                      hover
                      className="align-middle text-center"
                    >
                      <thead>
                        <tr>
                          <th className="text-dark small-text text-center">
                            Nome
                          </th>
                          <th className="text-dark small-text text-center">
                            Email
                          </th>
                          <th className="text-dark small-text text-center">
                            Numero di cellulare
                          </th>
                          <th className="text-dark small-text text-center">
                            Indirizzo
                          </th>
                          <th className="text-dark small-text text-center">
                            Ruolo
                          </th>
                          <th className="text-dark small-text text-center">
                            Ufficio assegnato
                          </th>
                          <th className="text-dark small-text text-center">
                            Stato
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredUsers.map((user) => (
                          <tr key={user.userId}>
                            <td className="text-dark small-text">
                              {user.name} {user.surname}
                            </td>
                            <td className="text-dark small-text">
                              {user.email}
                            </td>
                            <td className="text-dark small-text">
                              {user.phoneNumber}
                            </td>
                            <td className="text-dark small-text">
                              <p className="mb-0">
                                {user.streetAddress} {user.houseNumber}
                              </p>

                              <p className="mb-0">
                                {user.zipCode}, {user.city} {user.province}
                              </p>
                            </td>
                            <td className="text-dark small-text">
                              {user.roleNames.length > 0 ? (
                                <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                  {user.roleNames
                                    .map((role) => translateRole(role))
                                    .join(", ")}
                                  <div className="ms-auto">
                                    <Button
                                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center"
                                      disabled={loading}
                                      onClick={() => handleOpenRoleModal(user)}
                                    >
                                      <FaPencilAlt size={8} />
                                    </Button>
                                  </div>
                                </div>
                              ) : (
                                <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                  <p> - </p>
                                  <div className="ms-auto">
                                    <Button
                                      className="p-1 btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-auto"
                                      disabled={loading}
                                      onClick={() => handleOpenRoleModal(user)}
                                    >
                                      <IoMdAdd size={7} className="p-0" />
                                    </Button>
                                  </div>
                                </div>
                              )}
                            </td>
                            <td className="text-dark small-text">
                              <div
                                className="position-relative d-flex justify-content-center align-items-center"
                                style={{ minHeight: "50px" }}
                              >
                                {user.officeName ===
                                "Nessuna sede assegnata" ? (
                                  <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                    <p className="text-muted small-text text-nowrap">
                                      Nessuna sede assegnata
                                    </p>
                                    <Button
                                      className="p-1 btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-auto"
                                      disabled={loading}
                                      onClick={() =>
                                        handleOpenOfficeModal(user)
                                      }
                                    >
                                      <IoMdAdd size={7} className="p-0" />
                                    </Button>
                                  </div>
                                ) : (
                                  <div className="d-flex flex-column justify-content-center align-items-center w-100">
                                    <p className="text-dark small-text">
                                      {user.officeName}
                                    </p>
                                    <Button
                                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text ms-auto"
                                      disabled={loading}
                                      onClick={() =>
                                        handleOpenOfficeModal(user)
                                      }
                                    >
                                      <FaPencilAlt size={8} />
                                    </Button>
                                  </div>
                                )}
                              </div>
                            </td>
                            <td
                              className={`small-text text-center ${colorStatus(user.accountStatus)}`}
                            >
                              {translateStatus(user.accountStatus)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  </div>
                </Card>
              </>
            )}
          </Col>
        </Row>
        {/*MODALE PER + ASSEGNA NUOVO UFFICIO*/}
        <Modal
          show={showOfficeModal}
          onHide={() => setShowOfficeModal(false)}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title>
              {selectedUser?.officeName === "Nessuna sede assegnata"
                ? "Assegna sede"
                : "Modifica sede"}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {selectedUser?.officeName !== "Nessuna sede assegnata" && (
              <div className="mb-3">
                <p className="mb-1 text-muted small-text">Sede già assegnata</p>

                <p className="mb-0 fw-semibold">{selectedUser?.officeName}</p>
              </div>
            )}

            <Form.Group>
              <Form.Label>Nuova sede</Form.Label>

              <Form.Select
                value={selectedOfficeId}
                onChange={(e) => setSelectedOfficeId(e.target.value)}
              >
                <option value="">Seleziona una sede</option>

                {offices.map((office) => (
                  <option key={office.id} value={office.id}>
                    {office.name}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Modal.Body>

          <Modal.Footer>
            <Button
              className="btn-custom1"
              disabled={!selectedOfficeId || loading}
              onClick={handleSaveOffice}
            >
              Salva
            </Button>
            <Button
              variant="secondary"
              onClick={() => setShowOfficeModal(false)}
            >
              Annulla
            </Button>
          </Modal.Footer>
        </Modal>
        {/*MODALE PER I RUOLI */}
        <Modal
          show={showRoleModal}
          onHide={() => setShowRoleModal(false)}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title>
              {selectedUser && selectedUser.roleNames.length > 0
                ? "Modifica ruoli"
                : "Assegna ruoli"}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            {selectedUser && selectedUser.roleNames.length > 0 && (
              <div className="mb-3">
                <p className="mb-1 text-muted small-text">Ruoli attuali</p>

                <p className="mb-0 fw-semibold">
                  {selectedUser.roleNames
                    .map((role) => translateRole(role))
                    .join(", ")}
                </p>
              </div>
            )}

            <Form.Group>
              <Form.Label>Ruoli</Form.Label>

              {roles.map((role) => (
                <Form.Check
                  key={role.id}
                  type="checkbox"
                  label={translateRole(role.name)}
                  value={role.name}
                  checked={selectedRoles.includes(role.name)}
                  onChange={(e) => {
                    if (e.target.checked) {
                      setSelectedRoles((prev) => [...prev, role.name])
                    } else {
                      setSelectedRoles((prev) =>
                        prev.filter(
                          (selectedRole) => selectedRole !== role.name,
                        ),
                      )
                    }
                  }}
                />
              ))}
            </Form.Group>
          </Modal.Body>

          <Modal.Footer>
            <Button
              className="btn-custom1"
              disabled={selectedRoles.length === 0 || loading}
              onClick={handleSaveRoles}
            >
              Salva
            </Button>

            <Button variant="secondary" onClick={() => setShowRoleModal(false)}>
              Annulla
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  )
}

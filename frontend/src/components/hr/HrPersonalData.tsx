import { useEffect, useState } from "react"
import { HrNavbar } from "./HrNavbar"
import type { DocumentType, UserProfileResponseDTO } from "../../types/users"
import {
  getMyProfileDetails,
  updateMyProfile,
} from "../../services/userService"
import {
  Button,
  Card,
  Col,
  Container,
  Modal,
  Row,
  Spinner,
  Form,
} from "react-bootstrap"
import { FaPencilAlt } from "react-icons/fa"

export const HrPersonalData = () => {
  const [profile, setProfile] = useState<UserProfileResponseDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [showEditPersonalData, setShowEditPersonalData] = useState(false)

  const [name, setName] = useState("")
  const [surname, setSurname] = useState("")
  const [dateOfBirth, setDateOfBirth] = useState("")
  const [placeOfBirth, setPlaceOfBirth] = useState("")
  const [taxCode, setTaxCode] = useState("")

  const [showEditDocuments, setShowEditDocuments] = useState(false)

  const [documentNumber, setDocumentNumber] = useState("")
  const [documentType, setDocumentType] = useState("")
  const [issueDate, setIssueDate] = useState("")
  const [expirationDate, setExpirationDate] = useState("")

  const [documentFront, setDocumentFront] = useState<File | null>(null)
  const [documentBack, setDocumentBack] = useState<File | null>(null)
  const [taxCodeCardFront, setTaxCodeCardFront] = useState<File | null>(null)
  const [taxCodeCardBack, setTaxCodeCardBack] = useState<File | null>(null)

  const [streetAddress, setStreetAddress] = useState("")
  const [houseNumber, setHouseNumber] = useState("")
  const [zipCode, setZipCode] = useState("")
  const [city, setCity] = useState("")
  const [province, setProvince] = useState("")
  const [showAddressModal, setShowAddressModal] = useState(false)

  const [showContactsModal, setShowContactsModal] = useState(false)
  const [phoneNumber, setPhoneNumber] = useState("")
  const [iban, setIban] = useState("")

  const [saving, setSaving] = useState(false)

  //funzione per la data
  const formatDateForInput = (date: string) => {
    const [day, month, year] = date.split("/")

    return `${year}-${month}-${day}`
  }

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await getMyProfileDetails()
        setProfile(data)
      } catch (error) {
        console.error("Errore nel recupero del profilo:", error)

        alert(
          error instanceof Error
            ? error.message
            : "Errore nel recupero del profilo.",
        )
      } finally {
        setLoading(false)
      }
    }

    loadProfile()
  }, [])

  const translateDocumentType = (document: DocumentType | undefined) => {
    switch (document) {
      case "DRIVING_LICENSE":
        return "PATENTE DI GUIDA"

      case "IDENTITY_CARD":
        return "CARTA D'IDENTITÀ"

      case "PASSPORT":
        return "PASSAPORTO"
    }
  }

  //SALVATAGGIO MODIFICHE DATI PERSONALI
  const handleSavePersonalData = async () => {
    try {
      setSaving(true)

      const formData = new FormData()

      formData.append("name", name)
      formData.append("surname", surname)

      if (dateOfBirth) {
        formData.append("dateOfBirth", dateOfBirth)
      }

      formData.append("placeOfBirth", placeOfBirth)
      formData.append("taxCode", taxCode)

      const updatedProfile = await updateMyProfile(formData)

      setProfile(updatedProfile)

      setShowEditPersonalData(false)

      alert("Dati personali aggiornati correttamente.")
    } catch (error) {
      console.error("Errore durante la modifica del profilo:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante la modifica del profilo.",
      )
    } finally {
      setSaving(false)
    }
  }

  //SALVATAGGIO MODIFICHE DOCUMENTAZIONE
  const handleSaveDocuments = async () => {
    try {
      setSaving(true)

      const formData = new FormData()

      formData.append("documentNumber", documentNumber)
      formData.append("documentType", documentType)

      if (issueDate) {
        formData.append("issueDate", issueDate)
      }

      if (expirationDate) {
        formData.append("expirationDate", expirationDate)
      }

      if (documentFront) {
        formData.append("documentFront", documentFront)
      }

      if (documentBack) {
        formData.append("documentBack", documentBack)
      }

      if (taxCodeCardFront) {
        formData.append("taxCodeCardFront", taxCodeCardFront)
      }

      if (taxCodeCardBack) {
        formData.append("taxCodeCardBack", taxCodeCardBack)
      }

      const updatedProfile = await updateMyProfile(formData)

      setProfile(updatedProfile)
      setShowEditDocuments(false)

      alert("Documentazione aggiornata correttamente.")
    } catch (error) {
      console.error("Errore durante la modifica della documentazione:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante la modifica della documentazione.",
      )
    } finally {
      setSaving(false)
    }
  }

  // SALVATAGGIO MODIFICHE INDIRIZZO

  const handleSaveAddress = async () => {
    try {
      setSaving(true)

      const formData = new FormData()

      formData.append("streetAddress", streetAddress)
      formData.append("houseNumber", houseNumber)
      formData.append("zipCode", zipCode)
      formData.append("city", city)
      formData.append("province", province)

      const updatedProfile = await updateMyProfile(formData)

      setProfile(updatedProfile)
      setShowAddressModal(false)

      alert("Indirizzo aggiornato correttamente.")
    } catch (error) {
      console.error("Errore durante la modifica dell'indirizzo:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante la modifica dell'indirizzo.",
      )
    } finally {
      setSaving(false)
    }
  }

  //SALVATAGGIO MODIFICHE CONTATTI
  const handleSaveContacts = async () => {
    try {
      setSaving(true)

      const formData = new FormData()

      formData.append("phoneNumber", phoneNumber)
      formData.append("iban", iban)

      const updatedProfile = await updateMyProfile(formData)

      setProfile(updatedProfile)
      setShowContactsModal(false)

      alert("Contatti e IBAN aggiornati correttamente.")
    } catch (error) {
      console.error("Errore durante la modifica dei contatti:", error)

      alert(
        error instanceof Error
          ? error.message
          : "Errore durante la modifica dei contatti.",
      )
    } finally {
      setSaving(false)
    }
  }
  return (
    <>
      <HrNavbar />
      <Container
        fluid
        className="d-flex flex-column align-items-center flex-grow-1 my-4"
      >
        <Row className="w-100 justify-content-center align-items-center">
          <Col xs={12} md={11} className="ps-0">
            {/* TITOLO */}
            <div className="text-start mb-4">
              <h3 className="small-title text-dark mb-0">Dati personali</h3>
            </div>

            {/* CARDS */}
            {/*DATI ANAGRAFICI */}
            <Row className="g-4 align-items-evenly">
              <Col xs={12} md={6}>
                <Card className="border border-1 border-secondary rounded-2 p-3">
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="text-primary fw-bold mb-0">
                      Dati anagrafici
                    </h5>

                    <Button
                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center"
                      disabled={loading}
                      onClick={() => {
                        setName(profile?.name ?? "")
                        setSurname(profile?.surname ?? "")
                        setDateOfBirth(
                          profile?.dateOfBirth
                            ? formatDateForInput(profile.dateOfBirth)
                            : "",
                        )
                        setPlaceOfBirth(profile?.placeOfBirth ?? "")
                        setTaxCode(profile?.taxCode ?? "")

                        setShowEditPersonalData(true)
                      }}
                    >
                      <FaPencilAlt size={8} />
                    </Button>
                  </div>

                  <hr />
                  {loading ? (
                    <div className="d-flex justify-content-center align-items-center py-4">
                      <Spinner
                        animation="border"
                        role="status"
                        style={{ color: "#6f42c1" }}
                      >
                        <span className="visually-hidden">Caricamento...</span>
                      </Spinner>
                    </div>
                  ) : (
                    <div>
                      <div className="py-2 border-bottom">
                        <strong>Nome:</strong> {profile?.name}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Cognome:</strong> {profile?.surname}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Data di nascita:</strong> {profile?.dateOfBirth}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Luogo di nascita:</strong>{" "}
                        {profile?.placeOfBirth}
                      </div>

                      <div className="py-2">
                        <strong>Codice fiscale:</strong> {profile?.taxCode}
                      </div>
                    </div>
                  )}
                </Card>
              </Col>
              {/*MODALE DATI ANAGRAFICI */}
              <Modal
                show={showEditPersonalData}
                onHide={() => setShowEditPersonalData(false)}
                centered
              >
                <Modal.Header closeButton>
                  <Modal.Title>Modifica dati anagrafici</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                  <Form.Group className="mb-3">
                    <Form.Label>Nome</Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      defaultValue={profile?.name}
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Cognome</Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      defaultValue={profile?.surname}
                      value={surname}
                      onChange={(e) => setSurname(e.target.value)}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Data di nascita</Form.Label>
                    <Form.Control
                      className="input"
                      type="date"
                      defaultValue={
                        profile?.dateOfBirth
                          ? formatDateForInput(profile.dateOfBirth)
                          : ""
                      }
                      value={dateOfBirth}
                      onChange={(e) => setDateOfBirth(e.target.value)}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Luogo di nascita</Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      defaultValue={profile?.placeOfBirth}
                      value={placeOfBirth}
                      onChange={(e) => setPlaceOfBirth(e.target.value)}
                    />
                  </Form.Group>

                  <Form.Group>
                    <Form.Label>Codice fiscale</Form.Label>
                    <Form.Control
                      type="text"
                      defaultValue={profile?.taxCode}
                      className="input"
                      value={taxCode}
                      onChange={(e) => setTaxCode(e.target.value)}
                    />
                  </Form.Group>
                </Modal.Body>

                <Modal.Footer>
                  <Button
                    className="btn-custom1"
                    onClick={handleSavePersonalData}
                    disabled={saving}
                  >
                    {saving ? (
                      <>
                        <Spinner
                          animation="border"
                          size="sm"
                          className="me-2"
                        />
                        Salvataggio...
                      </>
                    ) : (
                      "Salva modifiche"
                    )}
                  </Button>
                  <Button
                    className="btn-custom2"
                    onClick={() => setShowEditPersonalData(false)}
                  >
                    Annulla
                  </Button>
                </Modal.Footer>
              </Modal>

              {/*INDIRIZZO */}
              <Col xs={12} md={6}>
                <Card className="border border-1 border-secondary rounded-2 p-3">
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="text-primary fw-bold mb-0">Indirizzo</h5>

                    <Button
                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center"
                      disabled={loading}
                      onClick={() => {
                        setStreetAddress(profile?.streetAddress ?? "")
                        setHouseNumber(profile?.houseNumber ?? "")
                        setZipCode(profile?.zipCode ?? "")
                        setCity(profile?.city ?? "")
                        setProvince(profile?.province ?? "")
                        setShowAddressModal(true)
                      }}
                    >
                      <FaPencilAlt size={8} />
                    </Button>
                  </div>

                  <hr />
                  {loading ? (
                    <div className="d-flex justify-content-center align-items-center py-4">
                      <Spinner
                        animation="border"
                        role="status"
                        style={{ color: "#6f42c1" }}
                      >
                        <span className="visually-hidden">Caricamento...</span>
                      </Spinner>
                    </div>
                  ) : (
                    <div>
                      <div className="py-2 border-bottom">
                        <strong>Via/Piazza:</strong> {profile?.streetAddress}{" "}
                        {profile?.houseNumber}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Cap:</strong> {profile?.zipCode}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Città:</strong> {profile?.city}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Provincia:</strong> {profile?.province}
                      </div>
                    </div>
                  )}
                </Card>
                {/* MODALE INDIRIZZO */}
                <Modal
                  show={showAddressModal}
                  onHide={() => setShowAddressModal(false)}
                  centered
                >
                  <Modal.Header closeButton>
                    <Modal.Title>Modifica indirizzo</Modal.Title>
                  </Modal.Header>

                  <Modal.Body>
                    <Form.Group className="mb-3">
                      <Form.Label>Via/Piazza</Form.Label>
                      <Form.Control
                        type="text"
                        className="input"
                        value={streetAddress}
                        onChange={(e) => setStreetAddress(e.target.value)}
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>Numero civico</Form.Label>
                      <Form.Control
                        type="text"
                        value={houseNumber}
                        onChange={(e) => setHouseNumber(e.target.value)}
                        className="input"
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>CAP</Form.Label>
                      <Form.Control
                        type="text"
                        value={zipCode}
                        onChange={(e) => setZipCode(e.target.value)}
                        className="input"
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>Città</Form.Label>
                      <Form.Control
                        type="text"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                        className="input"
                      />
                    </Form.Group>

                    <Form.Group>
                      <Form.Label>Provincia</Form.Label>
                      <Form.Control
                        type="text"
                        value={province}
                        onChange={(e) => setProvince(e.target.value)}
                        className="input"
                      />
                    </Form.Group>
                  </Modal.Body>

                  <Modal.Footer>
                    <Button
                      className="btn-custom1"
                      onClick={handleSaveAddress}
                      disabled={saving}
                    >
                      {saving ? (
                        <>
                          <Spinner
                            animation="border"
                            size="sm"
                            className="me-2"
                          />
                          Salvataggio...
                        </>
                      ) : (
                        "Salva modifiche"
                      )}
                    </Button>

                    <Button
                      className="btn-custom2"
                      onClick={() => setShowAddressModal(false)}
                    >
                      Annulla
                    </Button>
                  </Modal.Footer>
                </Modal>
              </Col>

              {/*DOCUMENTAZIONE */}
              <Col xs={12} md={6}>
                <Card className="border border-1 border-secondary rounded-2 p-3">
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="text-primary fw-bold mb-0">
                      Documentazione
                    </h5>

                    <Button
                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center"
                      disabled={loading}
                      onClick={() => {
                        setDocumentNumber(profile?.documentNumber ?? "")
                        setDocumentType(profile?.documentType ?? "")
                        setIssueDate(
                          profile?.issueDate
                            ? formatDateForInput(profile.issueDate)
                            : "",
                        )
                        setExpirationDate(
                          profile?.expirationDate
                            ? formatDateForInput(profile.expirationDate)
                            : "",
                        )

                        setDocumentFront(null)
                        setDocumentBack(null)
                        setTaxCodeCardFront(null)
                        setTaxCodeCardBack(null)

                        setShowEditDocuments(true)
                      }}
                    >
                      <FaPencilAlt size={8} />
                    </Button>
                  </div>

                  <hr />

                  <div>
                    <div className="py-2 border-bottom">
                      <strong>Tipo documento:</strong>{" "}
                      {translateDocumentType(profile?.documentType)}
                    </div>

                    <div className="py-2 border-bottom">
                      <strong>Numero:</strong> {profile?.documentNumber}
                    </div>

                    <div className="py-2 border-bottom">
                      <strong>Data di rilascio:</strong> {profile?.issueDate}
                    </div>

                    <div className="py-2 border-bottom">
                      <strong>Data di scadenza:</strong>{" "}
                      {profile?.expirationDate}
                    </div>

                    <div className="py-2 border-bottom">
                      <strong>Documento fronte:</strong>{" "}
                      {profile?.documentFrontUrl ? (
                        <a
                          href={profile.documentFrontUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary"
                        >
                          Visualizza documento
                        </a>
                      ) : (
                        <span className="text-muted">Non disponibile</span>
                      )}
                    </div>

                    <div className="py-2">
                      <strong>Documento retro:</strong>{" "}
                      {profile?.documentBackUrl ? (
                        <a
                          href={profile.documentBackUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary"
                        >
                          Visualizza documento
                        </a>
                      ) : (
                        <span className="text-muted">Non disponibile</span>
                      )}
                    </div>
                    <div className="py-2 border-bottom">
                      <strong>Tessera sanitaria fronte:</strong>{" "}
                      {profile?.taxCodeCardFrontUrl ? (
                        <a
                          href={profile.taxCodeCardFrontUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary"
                        >
                          Visualizza documento
                        </a>
                      ) : (
                        <span className="text-muted">Non disponibile</span>
                      )}
                    </div>

                    <div className="py-2">
                      <strong>Tessera sanitaria retro:</strong>{" "}
                      {profile?.taxCodeCardBackUrl ? (
                        <a
                          href={profile.taxCodeCardBackUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary"
                        >
                          Visualizza documento
                        </a>
                      ) : (
                        <span className="text-muted">Non disponibile</span>
                      )}
                    </div>
                  </div>
                </Card>
              </Col>
              {/*MODALE DOCUMENTAZIONE */}
              <Modal
                show={showEditDocuments}
                onHide={() => setShowEditDocuments(false)}
                centered
              >
                <Modal.Header closeButton>
                  <Modal.Title>Modifica documentazione</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                  <Form.Group className="mb-3">
                    <Form.Label>Tipo documento</Form.Label>

                    <Form.Select
                      value={documentType}
                      onChange={(e) => setDocumentType(e.target.value)}
                      className="input"
                    >
                      <option value="">Seleziona...</option>

                      <option value="IDENTITY_CARD">Carta d'identità</option>

                      <option value="DRIVING_LICENSE">Patente di guida</option>

                      <option value="PASSPORT">Passaporto</option>
                    </Form.Select>
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Numero documento</Form.Label>

                    <Form.Control
                      type="text"
                      value={documentNumber}
                      onChange={(e) => setDocumentNumber(e.target.value)}
                      className="input"
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Data di rilascio</Form.Label>

                    <Form.Control
                      type="date"
                      value={issueDate}
                      onChange={(e) => setIssueDate(e.target.value)}
                      className="input"
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Data di scadenza</Form.Label>

                    <Form.Control
                      type="date"
                      value={expirationDate}
                      onChange={(e) => setExpirationDate(e.target.value)}
                      className="input"
                    />
                  </Form.Group>

                  <hr />

                  <h6 className="fw-bold text-primary mb-3">Foto documento</h6>

                  <Form.Group className="mb-3">
                    <Form.Label>Fronte</Form.Label>

                    <Form.Control
                      className="input"
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const target = e.target as HTMLInputElement
                        setDocumentFront(target.files?.[0] ?? null)
                      }}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Retro</Form.Label>

                    <Form.Control
                      className="input"
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const target = e.target as HTMLInputElement
                        setDocumentBack(target.files?.[0] ?? null)
                      }}
                    />
                  </Form.Group>

                  <hr />

                  <h6 className="fw-bold text-primary mb-3">
                    Tessera sanitaria
                  </h6>

                  <Form.Group className="mb-3">
                    <Form.Label>Fronte</Form.Label>

                    <Form.Control
                      className="input"
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const target = e.target as HTMLInputElement
                        setTaxCodeCardFront(target.files?.[0] ?? null)
                      }}
                    />
                  </Form.Group>

                  <Form.Group>
                    <Form.Label>Retro</Form.Label>

                    <Form.Control
                      className="input"
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const target = e.target as HTMLInputElement
                        setTaxCodeCardBack(target.files?.[0] ?? null)
                      }}
                    />
                  </Form.Group>
                </Modal.Body>

                <Modal.Footer>
                  <Button
                    className="btn-custom1"
                    onClick={handleSaveDocuments}
                    disabled={saving}
                  >
                    {saving ? (
                      <>
                        <Spinner
                          animation="border"
                          size="sm"
                          className="me-2"
                        />
                        Salvataggio...
                      </>
                    ) : (
                      "Salva modifiche"
                    )}
                  </Button>
                  <Button
                    className="btn-custom2"
                    onClick={() => setShowEditDocuments(false)}
                  >
                    Annulla
                  </Button>
                </Modal.Footer>
              </Modal>

              {/*CONTATTI */}
              <Col xs={12} md={6}>
                <Card className="border border-1 border-secondary rounded-2 p-3">
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="text-primary fw-bold mb-0">
                      Contatti e Iban
                    </h5>

                    <Button
                      className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center"
                      disabled={loading}
                      onClick={() => {
                        setPhoneNumber(profile?.phoneNumber ?? "")
                        setIban(profile?.iban ?? "")
                        setShowContactsModal(true)
                      }}
                    >
                      <FaPencilAlt size={8} />
                    </Button>
                  </div>

                  <hr />
                  {loading ? (
                    <div className="d-flex justify-content-center align-items-center py-4">
                      <Spinner
                        animation="border"
                        role="status"
                        style={{ color: "#6f42c1" }}
                      >
                        <span className="visually-hidden">Caricamento...</span>
                      </Spinner>
                    </div>
                  ) : (
                    <div>
                      <div className="py-2 border-bottom">
                        <strong>Indirizzo email:</strong> {profile?.email}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Cellulare:</strong> {profile?.phoneNumber}
                      </div>

                      <div className="py-2 border-bottom">
                        <strong>Iban:</strong> {profile?.iban}
                      </div>
                    </div>
                  )}
                </Card>
              </Col>
              {/* MODALE CONTATTI E IBAN */}
              <Modal
                show={showContactsModal}
                onHide={() => setShowContactsModal(false)}
                centered
              >
                <Modal.Header closeButton>
                  <Modal.Title className="text-primary fw-bold">
                    Modifica contatti e IBAN
                  </Modal.Title>
                </Modal.Header>

                <Modal.Body>
                  <Form.Group className="mb-3">
                    <Form.Label>Cellulare</Form.Label>
                    <Form.Control
                      type="text"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                    />
                  </Form.Group>

                  <Form.Group>
                    <Form.Label>IBAN</Form.Label>
                    <Form.Control
                      type="text"
                      value={iban}
                      onChange={(e) => setIban(e.target.value)}
                    />
                  </Form.Group>
                </Modal.Body>

                <Modal.Footer>
                  <Button
                    className="btn-custom1"
                    onClick={handleSaveContacts}
                    disabled={saving}
                  >
                    {saving ? (
                      <>
                        <Spinner
                          animation="border"
                          size="sm"
                          className="me-2"
                        />
                        Salvataggio...
                      </>
                    ) : (
                      "Salva modifiche"
                    )}
                  </Button>

                  <Button
                    className="btn-custom2"
                    onClick={() => setShowContactsModal(false)}
                  >
                    Annulla
                  </Button>
                </Modal.Footer>
              </Modal>
            </Row>
          </Col>
        </Row>
      </Container>
    </>
  )
}

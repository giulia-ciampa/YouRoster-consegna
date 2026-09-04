import { useState, useEffect } from "react"
import "../../styles/dynamicSlogan.css"

const slogans = [
  "La tua pianificazione, in un click.",
  "Il tuo programma, sempre a portata di mano.",
  "Lavorare non è mai stato così semplice.",
  "Valorizziamo il tempo di chi lavora.",
  "Ottimizza i turni, massimizza il tempo.",
]

const DynamicSlogan = () => {
  const [index, setIndex] = useState(0)
  const [fade, setFade] = useState(true)

  useEffect(() => {
    const interval = setInterval(() => {
      setFade(false) // Inizia a scomparire
      setTimeout(() => {
        setIndex((prev) => (prev + 1) % slogans.length)
        setFade(true) // Riappare
      }, 500) // Tempo della transizione CSS
    }, 4000) // Ogni 4 secondi cambia

    return () => clearInterval(interval)
  }, [])

  return (
    <h3 className={`slogan-text ${fade ? "fade-in" : "fade-out"}`}>
      {slogans[index]}
    </h3>
  )
}

export default DynamicSlogan

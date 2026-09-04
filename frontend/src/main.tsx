import { createRoot } from "react-dom/client"
import App from "./App.tsx"
import "./styles/custom.scss"
import { Provider } from "react-redux"
import { store } from "../src/redux/store/store.ts"

createRoot(document.getElementById("root")!).render(
  <Provider store={store}>
    <App />
  </Provider>,
)

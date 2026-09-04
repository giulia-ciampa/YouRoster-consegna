import { configureStore } from "@reduxjs/toolkit"
import userReducer from "../reducers/userSlice"

export const store = configureStore({
  reducer: {
    user: userReducer,
  },
})

// Tipi utili per TypeScript (RootState e AppDispatch)
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

package com.github.ulanzetz.rento

import com.github.ulanzetz.rento.state.State
import korolev.Context

package object context {
  type AppContext[F[_]] = Context[F, State, Any]
}

package com.github.ulanzetz.rento.state

import korolev.Async
import korolev.state.{DeviceId, SessionId, StateManager, StateStorage}

// ignoring sessionId proxy
class DeviceIdStateStorage[F[_]: Async, S](underlying: StateStorage[F, S]) extends StateStorage[F, S] {
  def exists(deviceId: DeviceId, sessionId: SessionId): F[Boolean] =
    underlying.exists(deviceId, "")

  def create(deviceId: DeviceId, sessionId: SessionId, topLevelState: S): F[StateManager[F]] =
    underlying.create(deviceId, "", topLevelState)

  def get(deviceId: DeviceId, sessionId: SessionId): F[StateManager[F]] =
    underlying.get(deviceId, "")

  def remove(deviceId: DeviceId, sessionId: SessionId): Unit =
    underlying.remove(deviceId, "")
}

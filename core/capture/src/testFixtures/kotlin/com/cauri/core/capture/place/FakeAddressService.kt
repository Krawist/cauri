package com.cauri.core.capture.place

/** [AddressService] for tests: each query finds the addresses of [found], each position is at [address]. */
class FakeAddressService : AddressService {
    /** Addresses found by query. */
    val found = mutableMapOf<String, List<FoundAddress>>()

    /** Address of every position; `null` for unknown. */
    var address: FoundAddress? = null

    override suspend fun search(query: String) = found[query].orEmpty()

    override suspend fun addressAt(latitude: Double, longitude: Double) = address
}

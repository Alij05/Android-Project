package com.example.sickimfy.features.home.domain.usecase

import com.example.sickimfy.features.home.domain.repository.HomeFeed
import com.example.sickimfy.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): HomeFeed = repository.getHomeFeed()
}

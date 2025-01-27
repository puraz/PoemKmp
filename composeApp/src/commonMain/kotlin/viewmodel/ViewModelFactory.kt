package viewmodel

import data.PoemRepository

class ViewModelFactory(
    private val repository: PoemRepository
) {
    fun createHomeViewModel(): HomeViewModel = HomeViewModel(repository)
    fun createFavoritesViewModel(): FavoritesViewModel = FavoritesViewModel(repository)
} 
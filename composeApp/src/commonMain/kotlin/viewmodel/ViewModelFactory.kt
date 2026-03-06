package viewmodel

import AIServiceFactory
import data.PoemRepository

class ViewModelFactory(
    private val repository: PoemRepository
) {
    fun createHomeViewModel(): HomeViewModel = HomeViewModel(repository)
    fun createFavoritesViewModel(): FavoritesViewModel = FavoritesViewModel(repository)
    fun createSearchViewModel(): SearchViewModel = SearchViewModel(repository)
    fun createPoemAppreciationViewModel(): PoemAppreciationViewModel = PoemAppreciationViewModel(
        aiService = AIServiceFactory.createService(),
        poemRepository = repository
    )
    fun createAISearchViewModel(): AISearchViewModel = AISearchViewModel(
        poemRepository = repository,
        aiService = AIServiceFactory.createService()
    )
}

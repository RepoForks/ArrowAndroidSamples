package com.github.jorgecastillo.kotlinandroid.tagless.algebras.persistence

import arrow.Kind
import arrow.data.Try
import arrow.effects.Async
import arrow.syntax.either.right
import arrow.typeclasses.binding
import com.github.jorgecastillo.kotlinandroid.BuildConfig
import com.karumi.marvelapiclient.CharacterApiClient
import com.karumi.marvelapiclient.MarvelApiConfig
import com.karumi.marvelapiclient.model.CharacterDto
import com.karumi.marvelapiclient.model.CharactersQuery
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import javax.inject.Inject

class DataSource<F> @Inject constructor(val async: Async<F>){

    private val apiClient
        get() = CharacterApiClient(MarvelApiConfig.Builder(
                BuildConfig.MARVEL_PUBLIC_KEY,
                BuildConfig.MARVEL_PRIVATE_KEY).debug().build())

    private fun buildFetchHeroesQuery(): CharactersQuery =
            CharactersQuery.Builder.create().withOffset(0).withLimit(50).build()

    private fun fetchHero(heroId: String) =
            apiClient.getCharacter(heroId).response

    private fun fetchHeroes(query: CharactersQuery): List<CharacterDto> =
            apiClient.getAll(query).response.characters

    private fun <F, A, B> runInAsyncContext(
            f: () -> A,
            onError: (Throwable) -> B,
            onSuccess: (A) -> B, AC: Async<F>): Kind<F, B> {
        return AC.async { proc ->
            async(CommonPool) {
                val result = Try { f() }.fold(onError, onSuccess)
                proc(result.right())
            }
        }
    }

    fun fetchAllHeroes(): Kind<F, List<CharacterDto>> =
        async.binding {
            val query = buildFetchHeroesQuery()
            val result = runInAsyncContext(
                    f = { fetchHeroes(query) },
                    onError = { async.raiseError<List<CharacterDto>>(it) },
                    onSuccess = { async.pure(it) },
                    AC = async
            ).bind()
            result.bind()
        }


    fun fetchHeroDetails(heroId: String): Kind<F, CharacterDto> =
            async.binding {
                val result = runInAsyncContext(
                        f = { fetchHero(heroId) },
                        onError = { async.raiseError<CharacterDto>(it) },
                        onSuccess = { async.pure(it) },
                        AC = async
                ).bind()
                result.bind()
            }
}

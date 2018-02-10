package com.github.jorgecastillo.kotlinandroid.io.runtime.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import arrow.effects.extract
import com.github.jorgecastillo.kotlinandroid.R
import com.github.jorgecastillo.kotlinandroid.io.algebras.ui.Presentation
import com.github.jorgecastillo.kotlinandroid.io.algebras.ui.SuperHeroesListView
import com.github.jorgecastillo.kotlinandroid.io.algebras.ui.adapter.HeroesCardAdapter
import com.github.jorgecastillo.kotlinandroid.io.algebras.ui.model.SuperHeroViewModel
import kotlinx.android.synthetic.main.activity_main.heroesList

class SuperHeroListActivity : AppCompatActivity(), SuperHeroesListView {

  private lateinit var adapter: HeroesCardAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupList()
  }

  private fun setupList() {
    heroesList.setHasFixedSize(true)
    heroesList.layoutManager = LinearLayoutManager(this)
    adapter = HeroesCardAdapter(itemClick = {
      Presentation.onHeroListItemClick(this, it.heroId).extract().unsafeRunAsync {}
    })
    heroesList.adapter = adapter
  }

  override fun onResume() {
    super.onResume()
    Presentation.drawSuperHeroes(this).extract().unsafeRunAsync {}
  }

  override fun drawHeroes(heroes: List<SuperHeroViewModel>) = runOnUiThread {
    adapter.characters = heroes
    adapter.notifyDataSetChanged()
  }

  override fun showNotFoundError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.not_found, Snackbar.LENGTH_SHORT).show()
  }

  override fun showGenericError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.generic, Snackbar.LENGTH_SHORT).show()
  }

  override fun showAuthenticationError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.authentication, Snackbar.LENGTH_SHORT).show()
  }
}


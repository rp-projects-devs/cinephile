package com.cinephile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFiltersTest {

    @Test
    fun `des filtres vides ou composes d'espaces sont consideres comme vides`() {
        assertTrue(SearchFilters().isEmpty())
        assertTrue(SearchFilters(title = "   ", genre = " ").isEmpty())
    }

    @Test
    fun `un seul critere renseigne suffit pour que les filtres ne soient pas vides`() {
        assertFalse(SearchFilters(actor = "Kate Winslet").isEmpty())
        assertFalse(SearchFilters(year = "1997").isEmpty())
    }

    @Test
    fun `une annee de quatre chiffres est normalisee sans espaces`() {
        assertEquals("1997", SearchFilters(year = " 1997 ").normalizedYear())
        assertFalse(SearchFilters(year = "1997").hasInvalidYear())
    }

    @Test
    fun `une annee absente n'est pas une erreur`() {
        assertNull(SearchFilters(year = "").normalizedYear())
        assertFalse(SearchFilters(year = "").hasInvalidYear())
    }

    @Test
    fun `une annee mal formee est refusee`() {
        listOf("97", "19977", "abcd", "19 7").forEach { year ->
            val filters = SearchFilters(year = year)
            assertNull("annee \"$year\"", filters.normalizedYear())
            assertTrue("annee \"$year\"", filters.hasInvalidYear())
        }
    }
}

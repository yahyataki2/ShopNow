package com.example.shope_now

abstract class Produit(
    val code: String,
    val nom: String,
    var prixHT: Double,
    stock: Int
) {
    var stock: Int = stock
        private set

    abstract fun prixTTC(): Double

    fun retirerStock(quantite: Int): Boolean {
        if (quantite <= 0 || stock < quantite) {
            return false
        }

        stock -= quantite
        return true
    }

    fun ajouterStock(quantite: Int) {
        if (quantite > 0) {
            stock += quantite
        }
    }
}

interface Remisable {
    fun appliquerRemise(pourcentage: Double)
}

class ProduitElectronique(
    code: String,
    nom: String,
    prixHT: Double,
    stock: Int,
    val garantieMois: Int
) : Produit(code, nom, prixHT, stock), Remisable {

    override fun prixTTC(): Double {
        return prixHT * 1.20
    }

    override fun appliquerRemise(pourcentage: Double) {
        if (pourcentage in 0.0..100.0) {
            prixHT -= prixHT * pourcentage / 100
        }
    }
}

class ProduitAlimentaire(
    code: String,
    nom: String,
    prixHT: Double,
    stock: Int,
    val dateLimite: String
) : Produit(code, nom, prixHT, stock) {

    override fun prixTTC(): Double {
        return prixHT * 1.10
    }
}

data class Client(
    val id: Int,
    val nom: String,
    val ville: String
)

enum class StatutCommande {
    CREATED, PAID, SHIPPED, CANCELLED
}

class Commande(
    val numero: Int,
    val date: String,
    val client: Client,
    val produit: Produit,
    val quantite: Int
) {
    var statut: StatutCommande = StatutCommande.CREATED
        private set

    fun montantTotal(): Double {
        return produit.prixTTC() * quantite
    }

    fun payer(): Boolean {
        if (statut != StatutCommande.CREATED) {
            return false
        }

        if (!produit.retirerStock(quantite)) {
            return false
        }

        statut = StatutCommande.PAID
        return true
    }

    fun expedier(): Boolean {
        if (statut != StatutCommande.PAID) {
            return false
        }

        statut = StatutCommande.SHIPPED
        return true
    }

    fun annuler(): Boolean {
        if (statut == StatutCommande.SHIPPED ||
            statut == StatutCommande.CANCELLED
        ) {
            return false
        }

        if (statut == StatutCommande.PAID) {
            produit.ajouterStock(quantite)
        }

        statut = StatutCommande.CANCELLED
        return true
    }
}

class Boutique {

    private val produits = mutableListOf<Produit>()

    fun indiceProduit(code: String): Int {
        return produits.indexOfFirst { it.code == code }
    }

    fun ajouter(p: Produit): Boolean {
        if (indiceProduit(p.code) != -1) {
            return false
        }

        produits.add(p)
        return true
    }

    fun supprimer(code: String): Boolean {
        val indice = indiceProduit(code)

        if (indice == -1) {
            return false
        }

        produits.removeAt(indice)
        return true
    }

    fun supprimer(p: Produit): Boolean {
        return supprimer(p.code)
    }

    fun nombreProduitsElectroniques(): Int {
        return produits.count { it is ProduitElectronique }
    }
}

fun commandesDuClient(
    commandes: List<Commande>,
    client: String
): List<Commande> {
    return commandes.filter { it.client.nom == client }
}

fun totalPayees(commandes: List<Commande>): Double {
    return commandes
        .filter { it.statut == StatutCommande.PAID }
        .sumOf { it.montantTotal() }
}

fun totalAnnulees(commandes: List<Commande>): Double {
    return commandes
        .filter { it.statut == StatutCommande.CANCELLED }
        .sumOf { it.montantTotal() }
}

fun String.isValidPhoneNumber(): Boolean {
    return this.matches(Regex("\\+212[0-9]{9}"))
}

fun Double.toDh(): String {
    return "$this dhs"
}

fun filtrerCommandes(
    commandes: List<Commande>,
    critere: (Commande) -> Boolean
): List<Commande> {
    return commandes.filter(critere)
}

fun estPayee(c: Commande): Boolean {
    return c.statut == StatutCommande.PAID
}

fun jeuDeDonnees(): List<Commande> {

    val amine = Client(1, "Amine", "Safi")
    val sara = Client(2, "Sara", "Marrakech")
    val hicham = Client(3, "Hicham", "Rabat")
    val rania = Client(4, "Rania", "Agadir")

    val laptop = ProduitElectronique("E1", "Laptop", 5000.0, 100, 24)
    val phone = ProduitElectronique("E2", "Phone", 2000.0, 100, 12)
    val riz = ProduitAlimentaire("A1", "Riz", 20.0, 1000, "2027-01-01")
    val lait = ProduitAlimentaire("A2", "Lait", 10.0, 500, "2026-12-01")

    val c1 = Commande(1, "2026-10-01", amine, laptop, 1)
    c1.payer()

    val c2 = Commande(2, "2026-10-02", sara, phone, 1)
    c2.payer()

    val c3 = Commande(3, "2026-10-03", amine, riz, 10)
    c3.payer()

    val c4 = Commande(4, "2026-10-04", hicham, laptop, 2)
    c4.payer()
    c4.annuler()

    val c5 = Commande(5, "2026-10-05", sara, lait, 20)

    val c6 = Commande(6, "2026-10-06", rania, phone, 3)
    c6.payer()

    val c7 = Commande(7, "2026-10-07", amine, lait, 10)
    c7.annuler()

    val c8 = Commande(8, "2026-10-08", hicham, riz, 100)
    c8.payer()

    return listOf(c1, c2, c3, c4, c5, c6, c7, c8)
}

fun main() {

    val commandes = jeuDeDonnees()

    commandes.forEach {
        println("${it.client.nom} : ${it.montantTotal()}")
    }

    println(commandes.map { it.montantTotal() })

    println(
        commandes
            .filter { it.montantTotal() > 1000 }
            .map { it.numero }
    )

    println(
        commandes
            .sortedBy { it.montantTotal() }
            .map { it.numero }
    )

    println(
        commandes
            .sortedByDescending { it.montantTotal() }
            .map { it.numero }
    )

    println(
        commandes.any {
            it.statut == StatutCommande.CANCELLED
        }
    )

    println(
        commandes.all {
            it.statut == StatutCommande.PAID
        }
    )

    println(
        commandes
            .filter {
                it.client.ville == "Safi" &&
                        it.statut == StatutCommande.PAID
            }
            .sumOf { it.montantTotal() }
    )

    println(
        commandes
            .filter { it.statut == StatutCommande.PAID }
            .maxByOrNull { it.montantTotal() }?.client?.nom
    )

    println(
        commandes
            .filter { it.statut == StatutCommande.CANCELLED }
            .map { it.client.nom }
            .distinct()
    )

    println(
        commandes
            .filter { it.statut == StatutCommande.PAID }
            .sortedByDescending { it.montantTotal() }
            .take(3)
            .map {
                "${it.client.nom} : ${it.montantTotal()}"
            }
    )

    println(
        commandes
            .filter { it.statut == StatutCommande.PAID }
            .groupBy { it.client.ville }
            .mapValues { entry ->
                entry.value.sumOf { it.montantTotal() }
            }
    )

    println(commandesDuClient(commandes, "Amine").map { it.numero })

    println(totalPayees(commandes))
    println(totalAnnulees(commandes))

    println("+212600000000".isValidPhoneNumber())
    println("0600000000".isValidPhoneNumber())
    println("+212 600 000 000".isValidPhoneNumber())

    println(1200.0.toDh())

    println(
        filtrerCommandes(commandes) {
            it.montantTotal() > 1000
        }.map { it.numero }
    )

    println(
        filtrerCommandes(commandes, ::estPayee).map { it.numero }
    )

    println(
        filtrerCommandes(commandes) {
            it.client.ville == "Safi"
        }.map { it.numero }
    )

    val laptop = ProduitElectronique("E3", "Laptop Test", 5000.0, 10, 24)
    val remisable: Remisable = laptop

    remisable.appliquerRemise(20.0)
    println(laptop.prixHT)

    val boutique = Boutique()

    val laptop2 = ProduitElectronique("E4", "Laptop", 5000.0, 10, 24)
    val riz2 = ProduitAlimentaire("A3", "Riz", 20.0, 100, "2027-01-01")
    val phone2 = ProduitElectronique("E5", "Phone", 2000.0, 20, 12)

    println(boutique.ajouter(laptop2))
    println(boutique.ajouter(laptop2))
    println(boutique.ajouter(riz2))
    println(boutique.ajouter(phone2))

    println(boutique.indiceProduit("A3"))
    println(boutique.indiceProduit("ZZ"))

    println(boutique.nombreProduitsElectroniques())

    println(boutique.supprimer("A3"))
    println(boutique.supprimer("A3"))
    println(boutique.supprimer(laptop2))

    println(boutique.nombreProduitsElectroniques())
    println(boutique.indiceProduit("E5"))
}

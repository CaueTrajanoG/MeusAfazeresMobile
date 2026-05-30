package com.example.meusafazeres.network.firestore

import com.google.gson.annotations.SerializedName

/**
 * Firestore REST API requires a specific JSON format where each field
 * is wrapped with its type (stringValue, integerValue, booleanValue, etc.)
 */

data class FirestoreDocument<T>(
    val name: String? = null,
    val fields: T,
    val createTime: String? = null,
    val updateTime: String? = null
)

data class FirestoreListResponse<T>(
    val documents: List<FirestoreDocument<T>>? = null
)

data class StringField(val stringValue: String)
data class IntField(val integerValue: String) // Firestore REST uses string for numbers
data class BooleanField(val booleanValue: Boolean)
data class TimestampField(val timestampValue: String)

// Task DTO for Firestore
data class TaskFields(
    val titulo: StringField,
    val descricao: StringField,
    val prioridade: StringField,
    val status: StringField,
    val donoId: StringField,
    val dataCriacao: TimestampField,
    val dueDate: TimestampField? = null,
    val removido: BooleanField
)

// User DTO for Firestore
data class UserFields(
    val nome: StringField,
    val email: StringField,
    val dataCadastro: TimestampField
)

// --- Query DTOs ---

data class FirestoreQueryRequest(
    val structuredQuery: StructuredQuery
)

data class StructuredQuery(
    val from: List<CollectionSelector>,
    val where: Filter? = null,
    val orderBy: List<Order>? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

data class CollectionSelector(
    val collectionId: String
)

data class Filter(
    val compositeFilter: CompositeFilter? = null,
    val fieldFilter: FieldFilter? = null
)

data class CompositeFilter(
    val op: String, // "AND"
    val filters: List<Filter>
)

data class FieldFilter(
    val field: FieldReference,
    val op: String, // "EQUAL"
    val value: FirestoreValue
)

data class FieldReference(
    val fieldPath: String
)

data class FirestoreValue(
    val stringValue: String? = null,
    val booleanValue: Boolean? = null
)

data class Order(
    val field: FieldReference,
    val direction: String // "DESCENDING" or "ASCENDING"
)

data class FirestoreQueryResponse<T>(
    val document: FirestoreDocument<T>? = null,
    val readTime: String? = null
)

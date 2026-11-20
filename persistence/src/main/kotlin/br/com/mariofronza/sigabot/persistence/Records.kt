package br.com.mariofronza.sigabot.persistence

import org.jooq.Field
import org.jooq.Record

internal fun <T : Any> Record.notNull(field: Field<T?>): T =
    checkNotNull(get(field)) { "${field.qualifiedName} is NOT NULL in the schema" }

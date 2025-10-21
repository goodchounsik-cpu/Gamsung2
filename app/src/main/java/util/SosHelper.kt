package com.gamsung2.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object SosHelper {
    fun dial(ctx: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        ctx.startActivity(intent)
    }

    fun openRestroomMap(ctx: Context) {
        val uri = Uri.parse("geo:0,0?q=공중화장실")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        ctx.startActivity(intent)
    }
}

package com.step.admin.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Authentic FontAwesome Solid Vector Icons for STeP Admin Suite.
 */
object FontAwesomeIcons {

    private fun buildIcon(name: String, viewportWidth: Float, viewportHeight: Float, pathData: String): ImageVector {
        val nodes = PathParser().parsePathString(pathData).toNodes()
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight
        ).addPath(
            pathData = nodes,
            fill = SolidColor(Color.Black)
        ).build()
    }

    object Solid {
        val House: ImageVector by lazy {
            buildIcon(
                name = "fa-house",
                viewportWidth = 576f,
                viewportHeight = 512f,
                pathData = "M575.8 255.5c0 18-15 32.1-32 32.1h-32l.7 160.2c0 17-14 32.2-32 32.2h-64c-17 0-32-14.2-32-32.2v-96c0-17-14-32-32-32h-64c-17 0-32 15-32 32v96c0 18-15 32.2-32 32.2h-64c-17 0-32-15.2-32-32.2L96 287.6H64c-18 0-32-14.1-32-32.1 0-9 4-17 10-23l224-200c12-11 31-11 43 0l224 200c6 6 10 14 10 23z"
            )
        }

        val FilePen: ImageVector by lazy {
            buildIcon(
                name = "fa-file-pen",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M368.4 18.3L312 74.7 437.3 200l56.4-56.4c24.4-24.4 24.4-64 0-88.4L456.8 18.3c-24.4-24.4-64-24.4-88.4 0zM289.4 97.4L64 322.8V448h125.2l225.4-225.4L289.4 97.4z"
            )
        }

        val Timeline: ImageVector by lazy {
            buildIcon(
                name = "fa-timeline",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M64 64c0-17.7-14.3-32-32-32S0 46.3 0 64V448c0 17.7 14.3 32 32 32s32-14.3 32-32V64zm96 32c-17.7 0-32 14.3-32 32v64c0 17.7 14.3 32 32 32h288c17.7 0 32-14.3 32-32V128c0-17.7-14.3-32-32-32H160zm0 160c-17.7 0-32 14.3-32 32v64c0 17.7 14.3 32 32 32h192c17.7 0 32-14.3 32-32v-64c0-17.7-14.3-32-32-32H160z"
            )
        }

        val Headset: ImageVector by lazy {
            buildIcon(
                name = "fa-headset",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 32C132.3 32 32 132.3 32 256v112c0 26.5 21.5 48 48 48h32c17.7 0 32-14.3 32-32V256c0-17.7-14.3-32-32-32H80.5C92.4 146.9 166.4 80 256 80s163.6 66.9 175.5 144H400c-17.7 0-32 14.3-32 32v128c0 17.7 14.3 32 32 32h32c26.5 0 48-21.5 48-48V256c0-123.7-100.3-224-224-224zm-64 416c0 17.7 14.3 32 32 32h64c17.7 0 32-14.3 32-32s-14.3-32-32-32h-64c-17.7 0-32 14.3-32 32z"
            )
        }

        val Wallet: ImageVector by lazy {
            buildIcon(
                name = "fa-wallet",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H448c35.3 0 64-28.7 64-64V192c0-35.3-28.7-64-64-64H80c-8.8 0-16-7.2-16-16s7.2-16 16-16H448c17.7 0 32-14.3 32-32s-14.3-32-32-32H64zM416 272a32 32 0 1 1 0 64 32 32 0 1 1 0-64z"
            )
        }

        val User: ImageVector by lazy {
            buildIcon(
                name = "fa-user",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M224 256A128 128 0 1 0 224 0a128 128 0 1 0 0 256zm-45.7 48C79.8 304 0 383.8 0 482.3C0 498.7 13.3 512 29.7 512H418.3c16.4 0 29.7-13.3 29.7-29.7C448 383.8 368.2 304 269.7 304H178.3z"
            )
        }

        val CircleCheck: ImageVector by lazy {
            buildIcon(
                name = "fa-circle-check",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zM369 209L241 337c-9.4 9.4-24.6 9.4-33.9 0l-64-64c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l47 47L335 175c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9z"
            )
        }

        val CircleXmark: ImageVector by lazy {
            buildIcon(
                name = "fa-circle-xmark",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zM175 175c9.4-9.4 24.6-9.4 33.9 0l47 47 47-47c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9l-47 47 47 47c9.4 9.4 9.4 24.6 0 33.9s-24.6 9.4-33.9 0l-47-47-47 47c-9.4 9.4-24.6 9.4-33.9 0s-9.4-24.6 0-33.9l47-47-47-47c-9.4-9.4-9.4-24.6 0-33.9z"
            )
        }

        val TriangleExclamation: ImageVector by lazy {
            buildIcon(
                name = "fa-triangle-exclamation",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 32c14.2 0 27.3 7.5 34.5 19.8l216 368c7.3 12.4 7.3 27.7 .2 40.1S486.3 480 472 480H40c-14.3 0-27.4-7.7-34.7-20.1s-7.1-27.7 .2-40.1l216-368C228.7 39.5 241.8 32 256 32zm0 128c-13.3 0-24 10.7-24 24V296c0 13.3 10.7 24 24 24s24-10.7 24-24V184c0-13.3-10.7-24-24-24zm32 224a32 32 0 1 0 -64 0 32 32 0 1 0 64 0z"
            )
        }

        val ShieldCheck: ImageVector by lazy {
            buildIcon(
                name = "fa-shield-check",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 0c4.6 0 9.2 1 13.4 2.9L457.7 82.8c22 9.3 36.3 30.8 36.3 54.5v149.9c0 140.3-97.4 262.8-232.5 292.8c-3.6 .8-7.3 1.2-11 1.2s-7.4-.4-11-1.2C104.4 550 7 427.5 7 287.2V137.3c0-23.7 14.3-45.2 36.3-54.5L242.6 2.9C246.8 1 251.4 0 256 0zm105 217c9.4-9.4 9.4-24.6 0-33.9s-24.6-9.4-33.9 0l-95 95-47-47c-9.4-9.4-24.6-9.4-33.9 0s-9.4 24.6 0 33.9l64 64c9.4 9.4 24.6 9.4 33.9 0l112-112z"
            )
        }

        val BuildingColumns: ImageVector by lazy {
            buildIcon(
                name = "fa-building-columns",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M243.4 2.6l-224 96c-11.8 5-19.4 16.6-19.4 29.4s7.6 24.4 19.4 29.4l224 96c8 3.4 17.2 3.4 25.2 0l224-96c11.8-5 19.4-16.6 19.4-29.4s-7.6-24.4-19.4-29.4l-224-96c-8-3.4-17.2-3.4-25.2 0zM64 256H32v160h32V256zm96 0h-32v160h32V256zm128 0h-32v160h32V256zm96 0h-32v160h32V256zm96 0h-32v160h32V256zM0 480c0 17.7 14.3 32 32 32h448c17.7 0 32-14.3 32-32s-14.3-32-32-32H32c-17.7 0-32 14.3-32 32z"
            )
        }

        val FileLines: ImageVector by lazy {
            buildIcon(
                name = "fa-file-lines",
                viewportWidth = 384f,
                viewportHeight = 512f,
                pathData = "M64 0C28.7 0 0 28.7 0 64V448c0 35.3 28.7 64 64 64H320c35.3 0 64-28.7 64-64V160H256c-17.7 0-32-14.3-32-32V0H64zm192 0v128h128L256 0zM96 256c0-8.8 7.2-16 16-16H272c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16zm0 64c0-8.8 7.2-16 16-16H272c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16zm0 64c0-8.8 7.2-16 16-16H208c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16z"
            )
        }

        val ArrowRight: ImageVector by lazy {
            buildIcon(
                name = "fa-arrow-right",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M438.6 278.6c12.5-12.5 12.5-32.8 0-45.3l-160-160c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L338.8 224 32 224c-17.7 0-32 14.3-32 32s14.3 32 32 32l306.7 0-105.5 105.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0l160-160z"
            )
        }
    }
}

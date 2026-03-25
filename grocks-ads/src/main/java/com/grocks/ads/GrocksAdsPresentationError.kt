package com.grocks.ads

public enum class GrocksAdsPresentationError {
    NoPresenter,
    ;

    public val errorMessage: String
        get() = when (this) {
            NoPresenter -> "Не удалось найти Activity для показа."
        }
}

public class GrocksAdsPresentationException(
    public val presentationError: GrocksAdsPresentationError,
) : Exception(presentationError.errorMessage)

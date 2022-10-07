package eu.boxwork.dhbw.capturethecode.service

data class ServiceException(
    val code: Int,
    override val message:String
):Exception(message)

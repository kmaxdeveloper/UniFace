package com.uniface.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class StudentNotFoundException(message: String) : RuntimeException(message)
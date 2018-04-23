package com.z.framesetup

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HtmlControllerExample {
    @GetMapping("/front/*") fun index() = "index"
}
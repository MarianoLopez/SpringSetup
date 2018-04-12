package com.z.framesetup.Setup.Configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.framesetup.Setup.Data.Role
import com.z.framesetup.Setup.Data.RoleDAO
import com.z.framesetup.Setup.Data.User
import com.z.framesetup.Setup.Services.UserService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class OnBoot(private val userService: UserService,private val roleDAO: RoleDAO, private val mapper: ObjectMapper):ApplicationRunner{
    override fun run(args: ApplicationArguments?) {
        createAdminIfDoesNotExists()
        createTesterIfDoesNotExists()
    }

    fun createAdminIfDoesNotExists() {
        val adminRole = roleDAO.findById("ADMIN").let{ if(!it.isPresent)roleDAO.insert(Role("ADMIN", "ADMIN")) else it.get() }
        if(!userService.userDAO.findById("admin").isPresent) userService.insert(User("admin", "admin", "admin", true, listOf(adminRole))).also { println("admin user:\n\t" + mapper.writeValueAsString(it)) }
    }

    fun createTesterIfDoesNotExists() {
        val testerRole = roleDAO.findById("TESTER").let{ if(!it.isPresent)roleDAO.insert(Role("TESTER", "TESTER")) else it.get() }
        if(!userService.userDAO.findById("tester").isPresent) userService.insert(User("tester", "tester", "tester", true, listOf(testerRole))).also { println("tester user:\n\t" + mapper.writeValueAsString(it)) }
    }
}
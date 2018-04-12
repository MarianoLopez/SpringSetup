package com.z.framesetup.Setup.Controllers

import com.z.framesetup.Setup.Data.Role
import com.z.framesetup.Setup.Data.RoleDAO
import com.z.framesetup.Setup.Data.User
import com.z.framesetup.Setup.Services.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("user")
class UserController(private val userService: UserService) {
    @GetMapping fun findAll():List<User> = userService.userDAO.findAll()
    @GetMapping("{id}") fun findById(@PathVariable id:String) = userService.userDAO.findById(id)
    @PostMapping fun insert(@RequestBody user: User) = userService.insert(user)
    @PutMapping fun update(@RequestBody user: User) = userService.update(user)
}

@RestController
@RequestMapping("role")
class UserRoleController(private val roleDAO: RoleDAO){
    @GetMapping fun findAll():List<Role> = roleDAO.findAll()
    @GetMapping("{id}") fun findById(@PathVariable id:String) = roleDAO.findById(id)
    @PostMapping fun insert(@RequestBody role:Role) = roleDAO.insert(role)
    @PutMapping fun update(@RequestBody role:Role) = roleDAO.save(role)
}
package io.github.lukmccall.betterexclude

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModuleRootModel
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.vfs.impl.LightFilePointer
import com.intellij.openapi.vfs.pointers.VirtualFilePointer

class NodeModulesExcludeDirectoryPolicy : DirectoryIndexExcludePolicy {
  override fun getExcludeRootsForModule(rootModel: ModuleRootModel): Array<out VirtualFilePointer?> {
    val x = rootModel.getSourceRoots(false)
    val project = rootModel.module.project
    val projectDir = project.guessProjectDir() ?: return emptyArray()

    // Check if the project dir is a content root
    if (!rootModel.module.rootManager.contentRoots.contains(projectDir)) {
      return emptyArray()
    }

    var parent = projectDir.parent

    val containsPackageJson = parent?.findChild("package.json") != null
    if (!containsPackageJson) {
      return emptyArray()
    }

    val result = mutableListOf<VirtualFilePointer>()

    while (parent != null) {
      val nodeModules = parent.findChild("node_modules")
      if (nodeModules != null) {
        val pointer = LightFilePointer(nodeModules)
        result.add(pointer)
      }

      if (parent != projectDir.parent && parent.findChild("package.json") != null) {
        break
      }

      parent = parent.parent
    }

    return result.toTypedArray()
  }
}
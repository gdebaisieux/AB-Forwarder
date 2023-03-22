package com.ab.forwarder.domain.decoder.jwt

import com.ab.forwarder.domain.decoder.Decoder
import com.auth0.jwt.JWT
import org.springframework.stereotype.Component

@Component
class JwtDecoder : Decoder {

    override fun decode(raw: String, path: String): String {
        val decoded = JWT.decode(raw)
        val tree = getTreeFromPath(path)

        val rootClaim = decoded.getClaim(tree[0].name)
        return if (tree.size == 1) {
            rootClaim.asString()
        } else {
            // remove root claim
            val type = tree[0].type
            tree.removeAt(0)
            getValueFromTree(
                tree = tree,
                decoded = if (type == TreeType.MAP) {
                    rootClaim.asMap()
                } else {
                    val nodesAsList = rootClaim.asArray(Map::class.java) as Array<Map<String, Any>>
                    nodesAsList.filter { it.containsKey(tree[0].name) }[0]
                }
            )
        }
    }

    private fun getTreeFromPath(path: String): MutableList<TreeNode> {
        var tree = mutableListOf<TreeNode>()
        val splitPath = path.split(".")
        for (i in splitPath.indices) {
            var name = splitPath[i]
            val type =
                if (i < splitPath.size - 1) {
                    if (name.endsWith("[]")) {
                        name = name.replace("[]", "")
                        TreeType.ARRAY
                    } else {
                        TreeType.MAP
                    }
                } else {
                    TreeType.STRING
                }
            tree.add(TreeNode(name, type))
        }

        return tree
    }

    private fun getValueFromTree(tree: MutableList<TreeNode>, decoded: Any): String {
        return when (tree[0].type) {
            TreeType.STRING -> (decoded as Map<String, Any>)[tree[0].name].toString()
            TreeType.MAP -> {
                val name = tree[0].name
                tree.removeAt(0)
                getValueFromTree(tree, (decoded as Map<String, Any>)[name] as Map<String, Any>)
            }

            TreeType.ARRAY -> {
                val name = tree[0].name
                tree.removeAt(0)
                val listOfTreeNodes = (decoded as Map<String, Any>)[name] as List<Map<String, Any>>
                val mapForNode = listOfTreeNodes.filter { it.containsKey(tree[0].name) }[0]
                getValueFromTree(tree, mapForNode)
            }
        }
    }

    private data class TreeNode(val name: String, val type: TreeType)
    private enum class TreeType {
        MAP, ARRAY, STRING
    }
}
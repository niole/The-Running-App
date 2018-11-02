 package com.example.niolenelson.running.utilities

import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApi.newRequest
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng

 data class WayPoint(val node: LatLng, val length: Double, val children: List<WayPoint>) {
     fun cloneWithChildren(newChildren: List<WayPoint>): WayPoint {
         return WayPoint(node, length, newChildren)
     }
 }

class RouteGenerator(val routeError: Double, val start: LatLng, val routeLengthMeters: Double, val geoApiContext: GeoApiContext, val wayPoints: Array<LatLng>) {
    /**
     * previously fetched directions that are of suitable route length
     */
    private var directions: Map<Int, DirectionsResult> = mapOf()

    /**
     * do not contain start and end points
     */
    private var patterns: List<List<LatLng>> = listOf(listOf())

    private var index = -1

    init {
        setPatterns()
    }

    /**
     * use distance matrix api to get possible next steps
     * don't want to do to many API calls, so should just generate 1 at a time, upon user request
     * how to not double up on routes?
     * come up with a list of patterns and then chug through patterns
     */
    fun next(): List<LatLng> {
        if (index < patterns.size - 1) {
            index += 1
            while(index < patterns.size - 1 && !canPickRoute(index, patterns[index])) {
                index += 1
            }
            if (index < patterns.size) {
                return patterns[index]
            }
        }
        return listOf()
    }

    fun hasMoreRoutes(): Boolean {
        return index < patterns.size - 1
    }

    /**
     * gets real length of route and determines if can be suggested to the user
     */
    private fun canPickRoute(index: Int, route: List<LatLng>): Boolean {
        val result = newRequest(geoApiContext).origin(start).destination(start).waypoints(*route.toTypedArray()).optimizeWaypoints(true).await()
        directions.plus(Pair(index, result))

        val totalRoutes = result.routes.size
        if (totalRoutes > 0) {
            // TODO how to deal with different routes?
            val isSuitable = isDistanceSuitable(result.routes[0].legs.sumBy { leg -> leg.distance.inMeters.toInt() }.toDouble())
            return isSuitable
        }
        return false
    }

    private fun buildSuggestionTree(): List<WayPoint> {
        val viableStartPoints = wayPoints.filter { canPickPatternPoint(start, it, 0.0) }
        return viableStartPoints.mapIndexed { index, it ->
            val nextWayPoint = WayPoint(it, getTotalDistance(start, it, 0.0), listOf())
            addSuggestions(nextWayPoint, viableStartPoints.subList(0, index).plus(viableStartPoints.subList(index + 1, viableStartPoints.size)))
        }
    }

    /**
     * builds out a tree of possible paths
     * each node is a way point at which a user can keep going through or return home
     * distance is all based on "as the crow flies" distance
     */
    private fun addSuggestions(currEndPoint: WayPoint, remainingWayPoints: List<LatLng>): WayPoint {
        if (remainingWayPoints.isNotEmpty()) {
            val addables: List<LatLng> = remainingWayPoints.filter { canPickPatternPoint(currEndPoint.node, it, currEndPoint.length) }
            if (addables.isNotEmpty()) {
                return currEndPoint.cloneWithChildren(addables.mapIndexed { index, it ->
                    val newChild = WayPoint(it, getTotalDistance(currEndPoint.node, it, currEndPoint.length), listOf())
                    addSuggestions(newChild, addables.subList(0, index).plus(addables.subList(index + 1, addables.size)))
                })
            }
        }
        return currEndPoint
    }

    /**
     * creates general suggestion tree
     * suggestion tree likely contains out of the question lower bound routes, but
     * does not contain completely out of the question upper bound routes
     *
     * then filters suggestion tree into a list of more actionable as the crow flies routes
     * which will be suitable for iterating over and generating real routes lazily
     */
    private fun setPatterns() {
        val suggestionTree: List<WayPoint> = buildSuggestionTree()
        patterns = suggestionTree.flatMap { getSuggestedRoutes(listOf(it.node), it) }
    }

    private fun getSuggestedRoutes(soFar: List<LatLng>, tree: WayPoint): List<List<LatLng>> {
        if (tree.children.isEmpty()) {
            if (isDistanceSuitable(getTotalDistance(tree.node, start, tree.length))) {
                return listOf(soFar.plus(tree.node))
            }
        }

        return tree.children.flatMap {
            val nextRouteBase = soFar.plus(it.node)
            if (isDistanceSuitable(it.length)) {
                // cut this off as its own route and keep going
                return listOf(nextRouteBase).plus(getSuggestedRoutes(nextRouteBase, it))
            }
                // keep going
            getSuggestedRoutes(nextRouteBase, it)
        }
    }

    /**
     * determines whether a route distance is suitable for a real route, which
     * will be given to a user
     */
    private fun isDistanceSuitable(totalDistance: Double): Boolean {
        return totalDistance >= routeLengthMeters - routeError && totalDistance <= routeLengthMeters
    }

    /**
     * determines whether a distance is suitable for a route suggestion
     */
    private fun isDistanceSuggestionSuitable(totalDistance: Double): Boolean {
        return totalDistance <= routeLengthMeters
    }

    private fun getTotalDistance(lastPoint: LatLng, toPick: LatLng, distanceSoFar: Double): Double {
        return Haversine.distanceMeters(lastPoint.lat, lastPoint.lng, toPick.lat, toPick.lng) + distanceSoFar
    }

    /**
     * Determines whether a point can be picked for a route pattern suggestion based on whether you
     * can pick it and also return to start point
     */
    private fun canPickPatternPoint(lastPoint: LatLng, toPick: LatLng, distanceSoFar: Double): Boolean {
        val totalDistance = getTotalDistance(toPick, start, getTotalDistance(lastPoint, toPick, distanceSoFar))
        return isDistanceSuggestionSuitable(totalDistance)
    }

}
customr = [Base(biomes=False), EdgeLines()]
worlds["survival"] = "/home/minecraft/Scripts/mapmaker/mcserver/worlds/world"
worlds["creative"] = "/home/minecraft/Scripts/mapmaker/mcserver/worlds/plotworld"
worlds["limbo"] = "/home/minecraft/Scripts/mapmaker/mcserver/worlds/limbo"
worlds["lobby"] = "/home/minecraft/Scripts/mapmaker/mcserver/worlds/lobby"

renders["survival_north"] = {
    "world": "survival",
    "title": "Survival-North",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "upper-left",
}

renders["survival_south"] = {
    "world": "survival",
    "title": "Survival-South",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "lower-right",
}

renders["creative_north"] = {
    "world": "creative",
    "title": "Creative-North",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "upper-left",
}

renders["creative_south"] = {
    "world": "creative",
    "title": "Creative-South",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "lower-right",
}

renders["limbo_north"] = {
    "world": "limbo",
    "title": "Limbo-North",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "upper-left",
}   

renders["limbo_south"] = {
    "world": "limbo",
    "title": "Limbo-South",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "lower-right",
}

renders["lobby_north"] = {
    "world": "lobby",
    "title": "Lobby-North",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "upper-left",
}   

renders["lobby_south"] = {
    "world": "lobby",
    "title": "Lobby-South",
    "rendermode": customr,
    "dimension": "overworld",
    "northdirection" : "lower-right",
}

outputdir = "/home/www/mcpe/overviewer"

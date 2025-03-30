import kotlin.math.min
import kotlin.random.Random

data class Counter(var stones: Int, var roofs: Int)
data class Stone(val player: Int, var type: String)
{
    override fun toString(): String {
        var sign = ""
        sign = when (type)
        {
            "w" -> "*"
            "r" -> "'"
            else -> {
                " "
            }
        }
        return "$player$sign "
    }
}

data class Position(val i: Int, val j: Int)
{
    fun getLeft(): Position? {
        return if (j >= 1) {
            Position(i, j - 1);
        } else {
            null;
        }
    }

    fun getRight(): Position? {
        return if (j <= 3) {
            Position(i, j + 1);
        } else {
            null;
        }
    }

    fun getUpper(): Position? {
        return if (i >= 1) {
            Position(i - 1, j);
        } else {
            null;
        }
    }

    fun getLower(): Position? {
        return if (i <= 3) {
            Position(i + 1, j);
        } else {
            null;
        }
    }
}

object AnsiColor {
    const val RESET = "\u001B[0m"

    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    const val BOLD_BLACK = "\u001B[90m"
    const val BOLD_RED = "\u001B[91m"
    const val BOLD_GREEN = "\u001B[92m"
    const val BOLD_YELLOW = "\u001B[93m"
    const val BOLD_BLUE = "\u001B[94m"
    const val BOLD_PURPLE = "\u001B[95m"
    const val BOLD_CYAN = "\u001B[96m"
    const val BOLD_WHITE = "\u001B[97m"
}

fun main()
{
    println("${AnsiColor.RED}Добро пожаловать в игру 'Tak'!${AnsiColor.RESET}")
    println("Правила игры: Ваша задача построить дорогу с одного края до другого.\n" +
            "У Вас и вашего оппонента есть 21 камней и одна крыша для того, чтобы \n" +
            "сделать это! Ставьте свой камень в любом месте двумя способами: \n" +
            "обычный (плашмя) - и это будет часть дороги! \n" +
            "стеной (вертикально) - тогда это будет преграда! \n" +
            "На уже выложенные обычные камни (любого игрока) также можно положить \n" +
            "камень или поставить стену, сформировав стопку. На стену поставить камень \n" +
            "обычным способом уже нельзя! \n" +
            "В свой ход Вы можете поставить крышу - она распологается как камень \n" +
            "(и считается частью дороги), но на неё нельзя ничего поставить сверху \n" +
            "и она также может быть поставлена на стену (тем самым \"расплющив\" её \n" +
            "и превратив в обычный камень. \n" +
            "В свой ход можно сделать перемещение стопки! Вы можете переместить стопку \n" +
            "максимум на пять клеток (перемещение по горизонтали или по вертикали), \n" +
            "оставляя при этом на каждом шаге минимум один камень из этой стопки) \n" +
            "Выигрывает тот, кто первый построит дорогу, либо у кого будет больше \n" +
            "всего обычных камней (не внутри стопок). Приятной игры! \n")

    var playerStr: String? = null
    while (playerStr == null || playerStr.isEmpty())
    {
        println("Выберите свой номер: 1 или 2?")
        playerStr = readLine()
    }
    println("Начинаем игру....")
    val player = playerStr.toInt()
    val opponent = if (player == 1) 2 else 1

    var state: Array<Array<MutableList<Stone>>> = Array(5)
    {
        Array(5)
        {
            mutableListOf<Stone>()   //s - stone, w - wall, r - roof
        }
    }

    val counter: Counter = Counter(15, 1);
    val counterOpponent: Counter = Counter(15, 1);

    var winByCount: Int = 0;
    do
    {
        println(makeOutput(state))
        println("Варианы хода: \n" +
                "put i j type; \n" +
                "move i_from j_from direction(u, d, r, l) q1 q2 q3 ...; \n" +
                "")
        var continueGame = false;
        while (!continueGame) {
            val command = readLine()
            if (command == null || command.isEmpty()) {
                println("Пожалуйста, введите еще раз.")
                continue;
            }
            if (command != "stop") {
                val res = makeStep(state, counter, command, player);
                continueGame = res.second;
                state = res.first;
            } else {
                winByCount = player;
                break;
            }
        }
        println(counter);
        var gOver = gameOver(state, player);
        if (gOver == player) {
            println("Ура! Вы выиграли");
            break;
        } else if (gOver == opponent) {
            println("Противник выиграл(");
            break;
        }
        val tempCommand = if (Random.nextInt(1, 3) == 1) "put" else "move"
        println("Соперник совершает ход: ")
        var opponentsCommand = createOpponentsCommand(state, counterOpponent, tempCommand, opponent)
        if (opponentsCommand == "put") {
            opponentsCommand = createOpponentsCommand(state, counterOpponent, "put", opponent)
            if (opponentsCommand == "move") {
                println("Противник больше не может совршить ход.");
                winByCount = opponent;
                break;
            }
        } else if (opponentsCommand == "move") {
            opponentsCommand = createOpponentsCommand(state, counterOpponent, "move", opponent)
            if (opponentsCommand == "put") {
                println("Противник больше не может совршить ход.");
                winByCount = opponent;
                break;
            }
        }
        println("Команда противника " + opponentsCommand);
        continueGame = false;
        while (!continueGame) {
            val res = makeStep(state, counterOpponent, opponentsCommand, opponent);
            continueGame = res.second;
            state = res.first;
        }
        gOver = gameOver(state, player);
        if (gOver == player) {
            println("Ура! Вы выиграли");
            break;
        } else if (gOver == opponent) {
            println("Противник выиграл(");
            break;
        }

    } while (true)

    if (winByCount > 0) {
        var winning = 0;
        val secondPlayer = if (winByCount == 1) 2 else 1;
        val activeCount = countStones(state, winByCount);
        val notActiveCount = countStones(state, secondPlayer);
        if (activeCount >= notActiveCount) {
            winning = winByCount;
        } else {
            winning = secondPlayer;
        }
        if (winning == player) {
            println("Ура! Вы выиграли");
        } else {
            println("Противник выиграл(");
        }
    }
}

fun makeStep(statePrev: Array<Array<MutableList<Stone>>>, qPieces: Counter, command: String, player: Int): Pair<Array<Array<MutableList<Stone>>>, Boolean>
{
    val state = copyState(statePrev);
    val params = command.split(" ");
    val i: Int; val j: Int;
    try {
        i = params[1].toInt();
        j = params[2].toInt();
    } catch (ex: Exception) {
        println("неправильный ввод!");
        return Pair(statePrev, false);
    }
    val cell = state[i][j];
    when
    {
        params[0] == "put" -> {
            if ((params[3] == "s" || params[3] == "w")) {
                if (qPieces.stones == 0) {
                    println("Не хватает обычных камней!")
                    return Pair(statePrev, false);
                } else {
                    qPieces.stones -= 1;
                }
            }
            if ((params[3] == "r"))
            {
                if (qPieces.roofs == 0) {
                    println("Не хватает крыш!")
                    return Pair(statePrev, false);
                } else {
                    qPieces.roofs -= 1;
                }
            }
            if (cell.isNotEmpty()) {
                val result = check(params[3], cell[0].type);
                if (result.first)
                {
                    cell[0].type = result.second
                    cell.add(0, Stone(player, params[3]))
                } else {
                    println("Неправильный ввод, попробуйте снова (похоже, сюда такой камень не поставить)")
                    return Pair(statePrev, false);
                }
            } else {
                cell.add(0, Stone(player, params[3]))
            }
        }
        params[0] == "move" -> {
            val total: List<Int>; val totalSum: Int;
            try {
                total = params.drop(4).map { it.toInt() }
                totalSum = total.reduce { a, b -> a + b }
            } catch (ex: Exception) {
                println("Ошибка! В конце команды должно быть количество перемещаемых камней")
                return Pair(state, false);
            }

            if (totalSum > 5 || totalSum > state[i][j].count() || total.reduce{ a, b -> a * b} <= 0) {
                println("Неправильный ввод, попробуйте снова (что-то не так с количеством камней)");
                return Pair(state, false);
            } else if (cell.isEmpty())
            {
                println("Неправильный ввод, попробуйте снова (отсутствует стопка в данном месте)");
                return Pair(state, false);
            } else if (cell[0].player != player)
            {
                println("Неправильный ввод, попробуйте снова (это не ваша стопка)");
                return Pair(state, false);
            } else {
                var k = i; var l = j;
                val carryStones = cell.subList(0, totalSum).toMutableList();
                for (elem in 1..totalSum) cell.removeAt(0);
                val direction = when(params[3])
                {
                    "u" -> Pair(-1, 0);
                    "d" -> Pair(1, 0);
                    "r" -> Pair(0, 1);
                    "l" -> Pair(0, -1);
                    else -> {println("Неправильный ввод, попробуйте снова (несуществующее направление)"); return Pair(statePrev, false)}
                }
                //println("total count: ${total.count()}")
                for (g in 0..<total.count())
                {
                    k += direction.first; l += direction.second;
                    when
                    {
                        (k > 4) -> {k = 4}
                        (l > 4) -> {l = 4}
                        (k < 0) -> {k = 0}
                        (l < 0) -> {l = 0}
                    }
                    //println("count ind $k $l")
                    for (t in 0..<total[g])
                    {
                        val currentStone = carryStones.removeLast()
                        if (state[k][l].isNotEmpty())
                        {
                            val result = check(currentStone.type, state[k][l][0].type)
                            if (result.first)
                            {
                                state[k][l][0].type = result.second
                                state[k][l].add(0, currentStone)
                            } else {
                                println("Неправильный ввод, попробуйте снова (так фишки поставить невозможно)")
                                return Pair(statePrev, false);
                            }
                        } else {
                            state[k][l].add(0, currentStone)
                        }
                    }
                }
            }
        }
        else -> {
            println("Неправильный ввод, попробуйте снова (команда отсутствует)")
            return Pair(statePrev, false);
        }
    }
    return Pair(state, true);
}

fun createOpponentsCommand(state: Array<Array<MutableList<Stone>>>, qPieces: Counter, command: String, player: Int): String
{
    var putMovePossible = Pair<Boolean, Boolean>(true, true)
    val possiblePositions = mutableListOf<Pair<Int, Int>>()
    var finalCommand = ""
    if (command == "put") {
        var type: String
        val types = mutableListOf<String>("s", "w", "r")
        if (qPieces.roofs > 0 && qPieces.stones > 0) {
            type = if (Random.nextInt(1, 11) < 10) "s" else "r"
        } else if (qPieces.stones > 0) {
            type = "s"
            types.remove("r")
        } else if (qPieces.roofs > 0) {
            type = "r"
            types.remove("s")
            types.remove("w")
        } else {
            finalCommand = "move"
            return finalCommand
        }
        if (type == "s") {
            if (Random.nextInt(1, 11) < 6) {
                type = "w"
            } else {
                type = "s"
            }
        }

        while (true) {
            for (i in 0..<5) {
                for (j in 0..<5) {
                    if (state[i][j].isEmpty()) {
                        possiblePositions.add(Pair(i, j))
                    } else {
                        val checkingRes = check(type, state[i][j][0].type)
                        if (checkingRes.first) {
                            possiblePositions.add(Pair(i, j))
                        }
                    }
                }
            }
            if (possiblePositions.isEmpty()) {
                types.remove(type);
                if (types.isEmpty()) {
                    finalCommand = "move";
                    return finalCommand;
                } else {
                    type = types[0];
                }
            } else {
                break;
            }
        }
        val randPos = possiblePositions[Random.nextInt(0, possiblePositions.size)]
        finalCommand = "put ${randPos.first} ${randPos.second} ${type}"
    } else {
        for (i in 0..<5) {
            for (j in 0..<5) {
                if (state[i][j].isNotEmpty()) {
                    if (state[i][j][0].player == player) {
                        possiblePositions.add(Pair(i, j));
                    }
                }
            }
        }
        if (possiblePositions.isEmpty()) {
            finalCommand = "put";
            return finalCommand;
        }

        var direction: String? = null;
        var randPos: Pair<Int,Int> = Pair(-1, -1);
        var possibleDirections: MutableList<String>;
        var countDistribution = mutableListOf<Int>();
        while (direction.isNullOrEmpty()) {
            if (possiblePositions.isEmpty()) {
                finalCommand = "put";
                return finalCommand;
            }
            randPos = possiblePositions[Random.nextInt(0, possiblePositions.size)]
            possibleDirections = mutableListOf<String>("l", "r", "u", "d");
            countDistribution = mutableListOf<Int>();
            while (direction.isNullOrEmpty()) {
                if (possibleDirections.isEmpty()) {
                    break;
                }
                val ind = Random.nextInt(0, possibleDirections.size);
                direction = possibleDirections[ind];
                possibleDirections.removeAt(ind);
                val directionCount = when (direction) {
                    "l" -> randPos.second;
                    "u" -> randPos.first;
                    "r" -> 4 - randPos.second;
                    "d" -> 4 - randPos.first;
                    else -> 0;
                }
                val directionStep: Pair<Int, Int> = when (direction) {
                    "l" -> Pair(0, -1);
                    "u" -> Pair(-1, 0);
                    "r" -> Pair(0, 1);
                    "d" -> Pair(1, 0);
                    else -> Pair(0, 0);
                }
                if (directionCount == 0) {
                    direction = null;
                    continue;
                }
                val totalCount = min(Random.nextInt(1, 6), state[randPos.first][randPos.second].size);
                val countDistributionSize: Int;
                println("total count $totalCount direction count $directionCount");
                try {
                    countDistributionSize = Random.nextInt(1, min(totalCount, directionCount) + 1);
                } catch (ex: IllegalArgumentException) {
                    direction = null;
                    continue;
                }
                for (k in 1..countDistributionSize) {
                    countDistribution.add(1);
                }
                for (k in 1..totalCount - countDistributionSize) {
                    countDistribution[Random.nextInt(0, countDistributionSize)] += 1;
                }

                var i0 = randPos.first;
                var j0 = randPos.second;
                val top = state[randPos.first][randPos.second][0];
                var checked = true;
                for (k in 1..countDistributionSize) {
                    i0 += directionStep.first;
                    j0 += directionStep.second;
                    val cell = state[i0][j0];
                    if (cell.isNotEmpty()) {
                        if (cell[0].type != "s") {
                            if (k != countDistributionSize) {
                                checked = false;
                                break;
                            } else {
                                if (countDistribution[k] == 1) {
                                    if (top.type == "r") {
                                        if (cell[0].type == "r") {
                                            checked = false;
                                            break;
                                        }
                                    } else {
                                        checked = false;
                                        break;
                                    }
                                } else {
                                    checked = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!checked) {
                    direction = null;
                }
            }
        }
        finalCommand = "move ${randPos.first} ${randPos.second} ${direction}";
        for (k in 0..<countDistribution.size) {
            finalCommand += " ${countDistribution[k]}";
        }
    }

    return finalCommand;
}

fun gameOver(state: Array<Array<MutableList<Stone>>>, player: Int): Int
{
    if (findRoad(state, player)) {
        return player;
    } else {
        val secondPlayer = if (player == 1) 2 else 1;
        if (findRoad(state, secondPlayer)) {
            return secondPlayer;
        }
    }
    return 0;
}

fun countStones(state: Array<Array<MutableList<Stone>>>, player: Int): Int
{
    var count: Int = 0;
    for (i in 0..<state.size) {
        for (j in 0..<state[i].size) {
            if (state[i][j].isNotEmpty() && state[i][j][0].type == "s") {
                count += 1
            }
        }
    }
    return count;
}

fun findRoad(state: Array<Array<MutableList<Stone>>>, player: Int): Boolean
{
    val firstSide = mutableListOf<Position>();
    val secondSide = mutableListOf<Position>();
    var i = 0;
    for (j in 0..4) {
        if (isRoadStoneForPlayer(state[i][j], player)) {
            firstSide.add(Position(i, j));
        }
    }

    i = 4
    for (j in 0..4) {
        if (isRoadStoneForPlayer(state[i][j], player)) {
            secondSide.add(Position(i, j));
        }
    }

    if (firstSide.isNotEmpty() && secondSide.isNotEmpty()) {
        val tiles = mutableListOf<Position>();
        val visited = Array(5) { Array(5) { false } };
        tiles.addLast(firstSide.removeFirst());
        while (tiles.isNotEmpty()) {
            val pos = tiles.removeFirst();
            if (pos.i == 4) {
                return true;
            }
            pos.getRight()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            pos.getLeft()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            //preferred direction
            pos.getLower()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            if (tiles.isEmpty()) {
                if (firstSide.isNotEmpty()) {
                    val next = firstSide.removeFirst();
                    if (!visited[next.i][next.j]) {
                        tiles.addFirst(next);
                    }
                }
            }
        }
    }

    firstSide.clear();
    secondSide.clear();
    var j = 0;

    for (i0 in 0..4) {
        if (isRoadStoneForPlayer(state[i0][j], player)) {
            firstSide.add(Position(i0, j));
        }
    }

    j = 4
    for (i0 in 0..4) {
        if (isRoadStoneForPlayer(state[i0][j], player)) {
            secondSide.add(Position(i0, j));
        }
    }

    if (firstSide.isNotEmpty() && secondSide.isNotEmpty()) {
        val tiles = mutableListOf<Position>();
        val visited = Array(5) { Array(5) { false } };
        tiles.addLast(firstSide.removeFirst());
        while (tiles.isNotEmpty()) {
            val pos = tiles.removeFirst();
            if (pos.j == 4) {
                return true;
            }
            pos.getUpper()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            pos.getLower()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            //preferred direction
            pos.getRight()?.let {
                if (isRoadStoneForPlayer(state[it.i][it.j], player) && !visited[it.i][it.j]) {
                    tiles.addFirst(it);
                    visited[it.i][it.j] = true;
                }
            }

            if (tiles.isEmpty()) {
                if (firstSide.isNotEmpty()) {
                    val next = firstSide.removeFirst();
                    if (!visited[next.i][next.j]) {
                        tiles.addFirst(next);
                    }
                }
            }
        }
    }
    
    return false;
}

fun isRoadStoneForPlayer(tower: MutableList<Stone>, player: Int): Boolean {
    if (tower.isEmpty()) {
        return false;
    }
    if (tower[0].player == player && (tower[0].type == "s" || tower[0].type == "r")) {
        return true;
    } else {
        return false;
    }
}

fun copyState(state: Array<Array<MutableList<Stone>>>): Array<Array<MutableList<Stone>>>
{
    val rows = state.size
    val copy = Array(rows) { rowIndex ->
        val innerArraySize = state[rowIndex].size
        Array(innerArraySize) { colIndex ->
            state[rowIndex][colIndex].toMutableList()
        }
    }
    return copy
}

fun check(stoneToPlaceType: String, stoneOnTheBoardType: String): Pair<Boolean, String>
{
    var newStoneOnTheBoardType = stoneOnTheBoardType
    if (stoneToPlaceType == "r") {
        if (stoneOnTheBoardType == "r") return Pair(false, "-")
        if (stoneOnTheBoardType == "w") newStoneOnTheBoardType = "s"
        return Pair(true, newStoneOnTheBoardType)
    } else {
        if (stoneOnTheBoardType == "s")
        {
            return Pair(true, newStoneOnTheBoardType)
        } else { return Pair(false, "-") }
    }
}

fun makeOutput(state: Array<Array<MutableList<Stone>>>): String
{
    val head = " ${AnsiColor.BOLD_BLUE}|${AnsiColor.RESET} 0  1  2  3  4  ${AnsiColor.BOLD_BLUE}|${AnsiColor.RESET}\n"
    val horizontalLine = "${AnsiColor.BOLD_BLUE}--------------------${AnsiColor.RESET}\n"

    var mainLines = ""
    var notes = ""
    for (i in 0..4)
    {
        var line = "$i${AnsiColor.BOLD_BLUE}|${AnsiColor.RESET} ";
        for (j in 0..4)
        {
            val cell = state[i][j];
            if (cell.count() <= 1) {
                line += if (cell.isNotEmpty()) cell[0].toString() else "0  ";
            } else {
                line += AnsiColor.BOLD_RED + (if (cell.isNotEmpty()) cell[0].toString() else "0  ") + AnsiColor.RESET;
                notes += "($i, $j) - ";
                for (st in cell)
                {
                    notes += "${st.toString()}| ";
                }
                notes = notes.substring(0..notes.length - 2) + "\n";
            }
        }
        line += "${AnsiColor.BOLD_BLUE}|${AnsiColor.RESET}\n";
        mainLines += line
    }

    return  head +
            horizontalLine +
            mainLines +
            horizontalLine +
            notes +
            "\n" +
            "* - стена, ' - крыша"
}
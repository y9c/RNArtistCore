/**
 * This file is one of the file available from a new project directory created with the sscript rnartistcore.sh (see README for details).
 */

import io.github.fjossinet.rnartist.core.*

rnartist {
    file = "/project/outputs/bracket_notation.svg"
    ss {
        rna {
            sequence = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
        }
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }

    theme {
        details_lvl = 5

        color {
            type="A"
            value = "#A0ECF5"
        }

        color {
            type="a"
            value = "black"
        }

        color {
            type="U"
            value = "#9157E5"
        }

        color {
            type="G"
            value = "darkgreen"
        }

        color {
            type="C"
            value = "#E557E5"
        }

    }
}

rnartist {
    file = "/project/outputs/1gid.svg"
    ss {
        pdb {
            name = "B"
            file = "/project/inputs/1gid.pdb"
        }
    }
    theme {
        details_lvl = 5

        color {
            type="A"
            value = "#A0ECF5"
        }

        color {
            type="a"
            value = "black"
        }

        color {
            type="U"
            value = "#9157E5"
        }

        color {
            type="G"
            value = "darkgreen"
        }

        color {
            type="C"
            value = "#E557E5"
        }

    }
}

rnartist {
    file = "/project/outputs/RF02001.svg"
    ss {
        stockholm {
            file = "/project/inputs/RF02001.stockholm"
        }
    }

    theme {
        details_lvl = 3

        color {
            type="A"
            value = "#A0ECF5"
        }

        color {
            type="a"
            value = "black"
        }

        color {
            type="U"
            value = "#9157E5"
        }

        color {
            type="G"
            value = "darkgreen"
        }

        color {
            type="C"
            value = "#E557E5"
        }

    }
}
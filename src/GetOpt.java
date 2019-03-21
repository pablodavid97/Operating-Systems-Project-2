import java.util.StringTokenizer;


public class GetOpt {
    private static String VERSION = "$Id: GetOpt.java,v 1.2 2007/02/06 19:49:26 solomon Exp $";

    /** A record used in a table of option descriptions passed to a constructor
     * of GetOpt.  For example,
     * <pre>
     *   new LongOption("color", LongOption.REQ_ARG, 'c')
     * </pre>
     * describes a an option invoked as "--color=red"
     * equivalent to the short option "-c red".
     */
    public static class LongOption {
        private static String VERSION = "$Id: GetOpt.java,v 1.2 2007/02/06 19:49:26 solomon Exp $";

        /** The option name (for example, "color"). */
        public String name;

        /** A flag, one of {@link #NO_ARG}, {@link #REQ_ARG}, or {@link
         * #OPT_ARG}.
         */
        public int hasArg;

        /** The equivalent short (one-char) option name (for example 'c'). */
        public char val;

        /** Create a LongOption from its components.
         * @param name    the option name.
         * @param hasArg  NO_ARG, REQ_ARG, or OPT_ARG.
         * @param val     the equivalent short (one-char) option name.
         */
        public LongOption(String name, int hasArg, char val) {
            this.name = name;
            this.hasArg = hasArg;
            this.val = val;
        }
        /** Convert this option into a printable string.
         * @return the string.
         */
        public String toString() {
            return "{ \"" + name + "\", "
                + (hasArg == NO_ARG ? "NO_ARG"
                    : hasArg == REQ_ARG ? "REQ_ARG"
                    : hasArg == OPT_ARG ? "OPT_ARG"
                    : ("" + hasArg))
                + ", '" + val + "' }";
        }

    }
    /** A value for LongOption.has Arg meaning "option has no argument". */
    public static final int NO_ARG = 0;

    /** A value for LongOption.has Arg meaning "option has a required
     * argument".
     */
    public static final int REQ_ARG = 1;

    /** A value for LongOption.has Arg meaning "option has an optional
     * argument".
     */
    public static final int OPT_ARG = 2;

    /** The "program" (class) name (counterpart of argv[0] in Unix). */
    private String progname;

    /** The arguments to main (note that argv[0] is the first arg, not the
     * class name.
     */
    private String argv[];

    /** The flags argument currently in use.  Normally argv[optind-1], but
     * for flags that take separate arguments, optind may be larger.
     * For example, if flags a and b take arguments and argv is
     * { "-abc", "one", "two", "three" }, then after returning option 'b'
     * (with optarg "two"), we have optind==3, arg=="-abc"=argv[0], and
     * nextchar==2.  Also, if nextchar == -1, then arg will be reset to another
     * element of argv before being examined.
     * @see optind
     */
    private String arg;

    /** Short option flags. */
    private String shortopts;

    /** Table of long options. */
    private LongOption[] longopts;

    /** Flag to indicate that only long options should be recognized.
     * If true, '-' as well as '--' can introduce long-named options.
     */
    private boolean longonly;

    /** Index in argv of the next element to be scanned.
     * When nextOption() returns -1, this is the index of the first of the
     * non-option elements that the caller should itself scan (argv.length,
     * if there are no non-option arguments).
     * Otherwise, optind communicates from one call to the next how much of
     * argv has been scanned thus far.
     */
    public int optind = 0;

    /** The index in arg of the next option flag to be examined.
     * If nextchar < 0 or nextchar >= arg.length(), then arg has been
     * completely examined and argv[optind] should be examined next.
     */
    private int nextchar = -1;

    /** Index in longopts of a long-named option found.  It is only valid when
     * long-named option has been found in the most recent call to nextOpt().
     */
    public int longind;

    /** For communication from nextOpt() to the caller.  When nextOpt() finds
     * an option that takes an argument, the argument value is returned here.
     */
    public String optarg;

    /** If set to false, do not print messages to System.err for unrecognized
     * options.  By default, it is set to true.
     */
    public boolean opterr = true;

    /** Set to an option character which was unrecognized.
     */
    public char optopt = '?';

    /** Create a new option parser.
     * @param progname the name of the program (for error messages)
     * @param argv an array of command-line arguments
     * @param shortopts a string of short option flags.  A single colon
     * following a flag indicates it has a mandatory argument; a double colon
     * indicates an optional argument.
     * @param longopts a table of long option descriptors
     * @param longonly if true, indicates athat -flag should be treated as a
     * long flag, like --flag, rather than four short flags.
     */
    public GetOpt(String progname, String[] argv, String shortopts,
        LongOption[] longopts, boolean longonly)
    {
        this.progname = progname;
        this.argv = argv;
        this.shortopts = shortopts;
        this.longopts = longopts;
        this.longonly = longonly;
    }

    /** Create a new option parser.
     * Equivalent to GetOpt(progname, argv, shortopts, null, false)
     * @param progname the name of the program (for error messages)
     * @param argv an array of command-line arguments
     * @param shortopts a string of short option flags.  A single colon
     * following a flag indicates it has a mandatory argument; a double colon
     * indicates an optional argument.
     */
    public GetOpt(String progname, String[] argv, String shortopts) {
        this.progname = progname;
        this.argv = argv;
        this.shortopts = shortopts;
        this.longopts = null;
        this.longonly = false;
    }

    /** Create a new option parser.
     * Equivalent to GetOpt(progname, argv, shortopts, longopts, false)
     * @param progname the name of the program (for error messages)
     * @param argv an array of command-line arguments
     * @param shortopts a string of short option flags.  A single colon
     * following a flag indicates it has a mandatory argument; a double colon
     * indicates an optional argument.
     * @param longopts a table of long option descriptors
     */
    public GetOpt(String progname, String[] argv, String shortopts,
        LongOption[] longopts)
    {
        this.progname = progname;
        this.argv = argv;
        this.shortopts = shortopts;
        this.longopts = longopts;
        this.longonly = false;
    }


    /** Return the next option.  If there are no more options, return
     * -1.  Other information may be returned in the fields optind, longind,
     * optarg, and optopt.
     * @return -1 if there are no more options; otherwise a character
     * indicating which option was found (longopts[longind].val for long
     * options) or '?' for errors.
     */
    public int nextOpt() {
        optarg = null;
        if (nextchar < 0 || nextchar >= arg.length()) {
            // Advance to the next ARGV-element
            if (optind >= argv.length) {
                return -1;
            }
            arg = argv[optind++];

            // The special ARGV-element '--' means premature end of options.
            if (arg.equals("--")) {
                return -1;
            }

            // If we have come to a non-option, stop the scan.
            // An option is any word or length >=2 starting with a '-'.
            if (arg.length() < 2 || arg.charAt(0) != '-') {
                optind--;
                return -1;
            }

            nextchar = 1;
            if (longopts != null && arg.charAt(1)=='-') {
                nextchar++;
            }

            // Now nextchar "points to" the the first option flag in arg.
        }

        // Check for a long option
        // If longonly is true treat "-foo" like "--foo".  However,
        // "-f" is considered a short option if 'f' is in shortopts even
        // if longonly is true; otherwise, there woudl be no way to give the
        // -f short option.  On the other hand, if there's a long option
        // "foo" and the element is "-fo", do consider it an abbreviation for
        // the long option, just like "--fo" rather than "-f o".

        if (longopts != null && 
            (arg.charAt(1)=='-'     // "--foo"
            || (longonly &&
                    (arg.length() > 2   // -fo...
                    || shortopts.indexOf(arg.charAt(1))==-1
                        // -x (x not a short option)
                    )
                )
            )
        ) {
            boolean exact = false, ambig = false;
            int indfound = -1;
            int eq = arg.indexOf('=',nextchar);
            String optGiven =
                eq == -1 ? arg.substring(nextchar)
                    : arg.substring(nextchar, eq);
            String optWanted = null;

            // Test all long options for exact match or prefix match
            for (int i=0; i<longopts.length; i++) {
                optWanted = longopts[i].name;
                if (optWanted.startsWith(optGiven)) {
                    if (optWanted.length() == optGiven.length()) {
                        // exact match
                        indfound = i;
                        exact = true;
                        break;
                    }
                    if (indfound == -1) {
                        indfound = i;
                    } else {
                        ambig = true;
                    }
                }
            }
            if (ambig && !exact) {
                printErr("option", optGiven, "is ambiguous");
                nextchar = -1;
                optopt = 0;
                return '?';
            }
            if (indfound != -1) {
                int hasArg = longopts[indfound].hasArg;
                if (eq != -1) { // --foo=bar
                    if (hasArg == 0) {
                        // no argument allowed
                        String bad =
                            arg.charAt(1) == '-'
                                ? "--" + optWanted
                                : arg.charAt(0) + optWanted;
                        printErr("option", bad, "doesn't allow an argument");
                        nextchar = -1;
                        optopt = longopts[indfound].val;
                        return '?';
                    } else {
                        optarg = arg.substring(eq+1);
                    }
                } else if (hasArg == 1) { // argument required
                    if (optind < argv.length) {
                        // --foo bar
                        optarg = argv[optind++];
                    } else {
                        printErr("option", optGiven, "requires an argument");
                        nextchar = -1;
                        optopt = longopts[indfound].val;
                        return shortopts.charAt(0) == ':' ? ':' : '?';
                    }
                }
                // else argument is optional and option was --foo, so assume
                // there is no argument
                nextchar = -1;
                longind = indfound;
                return longopts[indfound].val;
            }

            // Can't find it as a long option.  If longonly was not specified,
            // or if the option starts with '--', or if this is not a valid
            // short option, there is an error.  Otherwise it is a short
            // option.
            if (!longonly
                || arg.charAt(1) == '-'
                || shortopts.indexOf(arg.charAt(nextchar)) == -1)
            {
                String bad =
                    arg.charAt(1) == '-'
                        ? "--" + optGiven
                        : argv[optind-1].charAt(0) + optGiven;
                printErr("unrecognized option", bad, "");
                nextchar = -1;
                optopt = 0;
                return '?';
            }
            // else fall through to short option code
        } // long option search

        // Short option search
        char c = arg.charAt(nextchar++);
        int pos = shortopts.indexOf(c);
        if (pos==-1 || c==':') {
            // The format of this message (and in particular, the word
            // "illegal" rather than "invalid" or some such) is dictated by
            // POSIX 1003.2.
            if (opterr) {
                System.err.println(progname + ": illegal option -- " + c);
            }
            optopt = c;
            return '?';
        }
        // GNU getopt for C has a bunch of special-case junk here for "W;"
        if (shortopts.length()>pos+1 && shortopts.charAt(pos+1) == ':') {
            if (shortopts.length()>pos+2 && shortopts.charAt(pos+2) == ':') {
                // optional argument
                if (nextchar < arg.length()) {
                    // -xfoo
                    optarg = arg.substring(nextchar);
                } else {
                    optarg = null;
                }
            } else {
                // required argument
                if (nextchar < arg.length()) {
                    // -xfoo
                    optarg = arg.substring(nextchar);
                } else if (optind >= argv.length) {
                    // The format of this message is dictated by POSIX 1003.2.
                    if (opterr)
                        System.err.println(progname
                            + ": option requires an argument -- " + c);
                    optopt = c;
                    c = shortopts.charAt(0) == ':' ? ':' : '?';
                } else {
                    // We already incremented optind once.  Increment it again
                    // when taking next element as an argument.
                    // Consume another word from argv to serve as the argument
                    optarg = argv[optind++];
                }
            }
            // In any case, the current flags argument is completely used up
            nextchar = -1;
        }
        // else, no argument
        return c;
    } // nextOpt

    /** A convenience routine to print an error to System.err if opterr is true.
     * The message has the form "progname: first-part `second-part' third-part".
     * @param a the first part of the message
     * @param b the middle part of the message
     * @param c the last part of the message
     */
    private void printErr(String a, String b, String c) {
        if (opterr) {
            System.err.println(progname + ": " + a + " `" + b + "' " + c);
        }
    }

    /** Helper function to main:  prints a message to System.err
     * @param o the message
     */
    private static void pl(Object o) { System.err.println(o); }

    /** Main program for testing.
     * @param ignore ignored
     */
    public static void main(String[] ignore) {
        String opts;
        String[] args;

        pl("--------- Testing short options");
        opts = "ab:c::";
        pl("shortopts = '" + opts + "'");
        args = tokenize("-aabarg1 -b arg2 -carg3 -q -c one two");
        // Results should be
        //    option a(null)
        //    option a(null)
        //    option b(arg1)
        //    option b(arg2)
        //    option c(arg3)
        //    GetOpt: illegal option -- q
        //    option q(null)
        //    option c(null)
        //    'one'
        //    'two'
            
        GetOpt opt = new GetOpt("GetOpt", args, opts);
        int c;
        while ((c = opt.nextOpt()) != -1) {
            pl("option " + (char)(c=='?' ? opt.optopt : c)
                + "(" + opt.optarg + ")");
        }
        pl("non-option args:");
        for (int i=opt.optind; i<args.length; i++) {
            pl("'" + args[i] + "'");
        }


        pl("--------- Testing long options");
        pl("shortopts = '" + opts + "'");
        LongOption[] lopts = new LongOption[] {
            new LongOption("argless", NO_ARG, 'x'),
            new LongOption("hasarg", REQ_ARG, 'y'),
            new LongOption("arg", OPT_ARG, 'z')
        };
        pl("longopts =");
        for (int i=0; i<lopts.length; i++) {
            pl("   " + lopts[i]);
        }
        args = tokenize("--argless --hasarg=arg1 --hasarg arg2 --arg"
            + " --arg=arg3 --a --foobar --argless=bad one two");
        // Results should be
        //    bar' '--argless=bad' 'one' 'two'
        //    option x[argless](null)
        //    option y[hasarg](arg1)
        //    option y[hasarg](arg2)
        //    option z[arg](null)
        //    option z[arg](arg3)
        //    GetOpt: option `a' is ambiguous
        //    option ?[arg](null)
        //    GetOpt: unrecognized option `--foobar'
        //    option ?[arg](null)
        //    GetOpt: option `--argless' doesn't allow an argument
        //    option ?[arg](null)
        //    'one'
        //    'two'
        opt = new GetOpt("GetOpt", args, opts, lopts);
        while ((c = opt.nextOpt()) != -1) {
            pl("option " + (char)c
                + "[" + lopts[opt.longind].name + "]"
                + "(" + opt.optarg + ")");
        }
        pl("non-option args:");
        for (int i=opt.optind; i<args.length; i++) {
            pl("'" + args[i] + "'");
        }
    } // main

    /** Helper procedure for main.
     * @param str a command-line-like string.
     * @return the result of breaking the line into an array of words
     */
    private static String[] tokenize(String str) {
        StringTokenizer st = new StringTokenizer(str);
        String[] res = new String[st.countTokens()];
        System.err.print("argv = ");
        for (int i=0; i<res.length; i++) {
            res[i] = st.nextToken();
            System.err.print(" '" + res[i] + "'");
        }
        System.err.println();
        return res;
    }
} // GetOpt

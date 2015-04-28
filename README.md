# RegexGenerator

This project contains the source code of a tool for generating regular expressions for text extraction:

1. automatically,
2. based only on examples of the desired behavior,
3. without any external hint about how the target regex should look like.

An online, interactive version of this engine is accessible at: [http://regex.inginf.units.it/](http://regex.inginf.units.it/)

RegexGenerator was developed at the [http://machinelearning.inginf.units.it] (Machine Learning Lab, University of Trieste, Italy).

The provided engine is a developement release(1) that implements the algorithms published in our articles (2):

* Bartoli, Davanzo, De Lorenzo, Medvet, Automatic Synthesis of Regular Expressions from Examples, IEEE Computer, 2014
* Bartoli, De Lorenzo, Medvet, Tarlao, Learning Text Patterns using Separate-and-Conquer Genetic Programming, 18th European Conference on Genetic Programming (EuroGP), 2015, Copenhagen (Denmark)

More details about the project can be found on the [http://machinelearning.inginf.units.it/news/newregexgeneratortoolonline](Machine Learning Lab news pages).

We hope that you find this code instructive and useful for your research or study activity.

If you use our code in your reasearch please cite our work and please share back your enhancements, fixes and 
modifications.

## Project Structure

The RegexGenerator project is organized in three NetBeans Java subprojects:

* Console Regex Turtle:  cli frontend for the GP engine
* MaleRegexTurtle:       provides the regular expression tree representation
* RandomRegexTurtle:     GP search engine 

## Other Links

Machine Learning Lab, twitter account [https://twitter.com/MaleLabTs](https://twitter.com/MaleLabTs)

---

(1) This is a developement version branch which differs from the cited works.

(2) BibTeX format:

@article{bartoli2013automatic,
  title={Automatic synthesis of regular expressions from examples},
  author={Bartoli, Alberto and Davanzo, Giorgio and De Lorenzo, Andrea and Medvet, Eric and Sorio, Enrico},
  year={2013},
  publisher={IEEE}
}

@incollection{bartoli2015learning,
  title={Learning Text Patterns Using Separate-and-Conquer Genetic Programming},
  author={Bartoli, Alberto and De Lorenzo, Andrea and Medvet, Eric and Tarlao, Fabiano},
  booktitle={Genetic Programming},
  pages={16--27},
  year={2015},
  publisher={Springer}
}

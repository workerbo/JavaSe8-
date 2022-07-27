![img](https://gitee.com/workerbo/gallery/raw/master/2020/git.jpg)



`git fetch`是将远程主机的最新内容拉到本地，用户在检查了以后决定是否合并到工作本机分支中。

而`git pull` 则是将远程主机的最新内容拉下来后直接合并，即：`git pull = git fetch + git merge`，这样可能会产生冲突，需要手动解决。



reset 还有一个非常重要的特性，就是**真正的后退一个版本**。

什么意思呢？比如说当前提交，你已经推送到了远程仓库；现在你用 reset 撤回了一次提交，此时本地 git 仓库要落后于远程仓库一个版本。此时你再 push，远程仓库会拒绝，要求你先 pull。

如果你需要远程仓库也后退版本，就需要 -f 参数，强制推送，这时本地代码会覆盖远程代码。

注意，-f 参数非常危险！如果你对 git 原理和命令行不是非常熟悉，切记不要用这个参数。

## git revert

- revert 与 reset 的作用一样，都是恢复版本，但是他们两者的实现方式不同。
- 简单来说 reset 是直接恢复到上一个提交，工作区的代码自然也恢复到上一次提交的代码；而 revert 是新增一个提交，但是这个提交是使用上一次的代码。
- 因此，他两恢复后的代码是一致的，区别是一个新增提交（revert），一个是回退提交（reset）
- 正因为 revert 永远是在新增提交，因此本地仓库版本永远不可能落后于远程仓库，可以直接推送到远程仓库，故而解决了 reset 之后需要加 -f 参数的问题，提高了安全性。

##### revert merge commit

revert merge commit 有一些不同，这时需要添加 -m 选项以代表这次 revert 的是一个 merge commit

但如果直接使用 git revert <commit id>，git 也不知道到底要撤除哪一条分支上的内容，这时需要指定一个 parent number 标识出"主线"，主线的内容将会保留，而另一条分支的内容将被 revert。

如上面的例子中，从 git show 命令的结果中可以看到，merge commit 的 parent 分别为 ba25a9d 和 1c7036f，其中 ba25a9d 代表 master 分支（从图中可以看出），1c7036f 代表 will-be-revert 分支。需要注意的是 -m 选项接收的参数是一个数字，数字取值为 1 和 2，也就是 Merge 行里面列出来的第一个还是第二个。

保留主分支，应该设置主分支为主线


[git rest命令的原理](https://blog.csdn.net/u013064585/article/details/94831107)

reset current branch to here  重置到某条记录并且不会保留这条记录之后的日志、然后强制push

reset 三种模式

![img](https://upload-images.jianshu.io/upload_images/4428238-fcad08ebe26933a6.png?imageMogr2/auto-orient/strip|imageView2/2/w/638/format/webp)

当我们想合并「当前节点」与「reset目标节点」之间不具太大意义的 **commit** 记录(可能是阶段性地频繁提交)時，可以考虑使用 **Soft Reset** 来让 **commit** 演进线图较为清晰点。

**--hard**：重置位置的同时，直接将 **working Tree工作目录**、 **index 暂存区**及 **repository** 都重置成目标**Reset**节点的內容,所以效果看起来等同于清空暂存区和工作区。

**--soft**：重置位置的同时，保留**working Tree工作目录**和**index暂存区**的内容，只让**repository**中的内容和 **reset** 目标节点保持一致，因此原节点和**reset**节点之间的【差异变更集】会放入**index暂存区**中(**Staged files**)。所以效果看起来就是工作目录的内容不变，暂存区原有的内容也不变，只是原节点和**Reset**节点之间的所有差异都会放到暂存区中。

**--mixed（默认）**：重置位置的同时，只保留**Working Tree工作目录**的內容，但会将 **Index暂存区** 和 **Repository** 中的內容更改和reset目标节点一致，因此原节点和**Reset**节点之间的【差异变更集】会放入**Working Tree工作目录**中。所以效果看起来就是原节点和**Reset**节点之间的所有差异都会放到工作目录中。

### reset 的本质：移动 HEAD 以及它所指向的 branch







revert merge commit 有一些不同，这时需要添加 `-m` 选项以代表这次 revert 的是一个 merge commit

但如果直接使用 git revert ，git 也不知道到底要撤除哪一条分支上的内容，这时需要指定一个 parent number 标识出"主线"，主线的内容将会保留，而另一条分支的内容将被 revert。

修好之后想重新合并到 master，直觉上只需要再 merge 到 master 即可（或者使用 cherry-pick），像这样：

但需要注意的是，这 不能 得到我们期望的结果。因为 d 和 e 两个提交曾经被丢弃过，如此合并到 master 的代码，并不会重新包含 d 和 e 两个提交的内容，相当于只有 goudan/a-cool-feature 上的新 commit 被合并了进来，而 goudan/a-cool-feature 分支之前的内容，依然是被 revert 掉了。

所以，如果想恢复整个 goudan/a-cool-feature 所做的修改，应该先把 G revert 掉：



`git cherry-pick`命令的作用，就是将指定的提交（[commit](https://so.csdn.net/so/search?q=commit)）应用于其他分支。



git revert 和 git reset的区别 
\1. git revert是用一次新的commit来回滚之前的commit，git reset是直接删除指定的commit。 
\2. 在回滚这一操作上看，效果差不多。但是在日后继续merge以前的老版本时有区别。因为git revert是用一次逆向的commit“中和”之前的提交，因此日后合并老的branch时，导致这部分改变不会再次出现，但是git reset是之间把某些commit在某个branch上删除，因而和老的branch再次merge时，这些被回滚的commit应该还会被引入。 
\3. git reset 是把HEAD向后移动了一下，而git revert是HEAD继续前进，只是新的commit的内容和要revert的内容正好相反，能够抵消要被revert的内容。





git配置同时使用 Gitlab、Github、Gitee(码云) 共存的开发环境  https://www.jianshu.com/p/68578d52470c





1.配置好ssh用于远程
https://www.cnblogs.com/gavincoder/articles/9071959.html
https://www.jikewenku.com/276.html

冲突时合并新提交在同一文件同一行同时出现了修改。


常用回滚命令！
https://blog.csdn.net/asoar/article/details/84111841

1、在工作区的代码

git checkout -- a.txt   # 丢弃某个文件，或者
git checkout -- .       # 丢弃全部

2、代码git add到缓存区，并未commit提交

git reset HEAD .  或者
git reset HEAD a.txt